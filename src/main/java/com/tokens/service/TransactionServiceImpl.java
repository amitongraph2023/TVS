package com.tokens.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tokens.models.Location;
import com.tokens.models.MasterKey;
import com.tokens.models.Transaction;
import com.tokens.models.TransactionStatus;
import com.tokens.models.TransactionStatusLogs;
import com.tokens.models.User;
import com.tokens.repository.LocationRepository;
import com.tokens.repository.MasterKeyRepository;
import com.tokens.repository.TransactionRepository;
import com.tokens.repository.TrasactionStatusLogsRepository;
import com.tokens.repository.UserRepository;
import com.tokens.request.CloudRequest;
import com.tokens.response.CloudResponse;
import com.tokens.utils.CodeGenerator;
import com.tokens.utils.JwtUtil;

@Service
public class TransactionServiceImpl implements TransactionService {

	Logger logger = LoggerFactory.getLogger(TransactionServiceImpl.class);
	
	@Autowired
	TvsCsvReader reader;

	@Autowired
	TransactionRepository transactionRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	JwtUtil tokenGenerator;

	@Autowired
	MasterKeyRepository masterKeyRepository;

	@Autowired
	LocationRepository locationRepository;

	@Autowired
	TrasactionStatusLogsRepository transactionStatusLogsRepository;

	@Override
	public CloudResponse generateTransactionToken(CloudRequest request) {
		CloudResponse response = null;
		Transaction transaction = null;
		try {
			String token = "";

			saveLocationFromCSV();
			
			if (request.getMerchantId() != null) {
				int merchantId = Integer.parseInt(request.getMerchantId());
				Location location = locationRepository.findByMerchantId(merchantId);
				if (location == null) {
					return new CloudResponse("", (long)0, "MerchantId doesn't Exists");
				}
			}

			MasterKey key = masterKeyRepository.findMasterKeyBySystemId(request.getSystemId());
			if (key != null && key.getMasterKey() != null) {
				token = CodeGenerator.generateHashCode(key.getMasterKey());
			} else {
				logger.error("cannot generate Token Without MasterKey");
				return new CloudResponse("", Long.MIN_VALUE, "SystemId doesn't Exists");
			}

			// saving transactions in DB
			transaction = saveTransaction(request, token);
			
			if (transaction != null) {
				response = new CloudResponse(token, transaction.getTransactionId(), "");
			} else {
				return new CloudResponse("", (long) 0, "Something got wrong, Exception occured. Try after some Time");
			}
		} catch (Exception e) {
			logger.error("Exception occurred while generation Token, Error : "+e.getMessage());
		}
		return response;
	}

	@Transactional
	public Transaction saveTransaction(CloudRequest req, String token) {

		Transaction transaction = null;
		String exceptionMessage = validateCloudRequest(req);
		logger.info("exceptionMessage : " + exceptionMessage);
		if (exceptionMessage.length() == 0) {
			try {


				transaction = new Transaction( Long.parseLong(req.getTransactionId()),token, Integer.parseInt(req.getCustomerId()),
						Double.parseDouble(req.getAmount()), req.getCreatedDate(),
						Integer.parseInt(req.getMerchantId()), Integer.parseInt(req.getPosId()),
						req.getCardNumber(), req.getSourceIp(), req.getGpsLocation(), req.getSystemId());

				transaction = transactionRepository.save(transaction);

				if (checkLocationIfExsists(transaction.getMerchantId())) {
					transactionRepository.save(transaction);
				} else {
					throw new Exception("Location Does Not Esists");
				}

				// need to save data in location and pos table here - not sure for it
				// as we will be having merchant details in our db to validate req with merchant
				// Id and name
				// so we need it before in order to valdiate and no need to save location then
				// saveLocation(transaction);

			} catch (Exception ex) {
				logger.error("Exception occurred while saving Transaction, Error :" + ex.getMessage());
			}

		} else {
			logger.error("Invalidate Request, Error :" + exceptionMessage);
		}
		return transaction;
	}

	@Override
	public List<Transaction> logsTransactionToken(int userId) {
		User user = userRepository.findById(userId).get();
		List<Transaction> transactionTokenLog = transactionRepository.findTransactionLogs(user.getSystemId());
		return transactionTokenLog;
	}

	public String validateCloudRequest(CloudRequest req) {
		String exceptionMessage = "";

		if (req.getTransactionId() == null) {
			exceptionMessage = "TransactionId is null";
			return exceptionMessage;
		} else if (req.getAmount() == null || Double.parseDouble(req.getAmount()) < 0) {

			exceptionMessage = "Amount is null or less than 0";
			return exceptionMessage;
		} else if (req.getCardNumber() == null) {

			exceptionMessage = "CardNumber is null";
			return exceptionMessage;
		} else if (req.getCustomerId() == null) {

			exceptionMessage = "CustomerId is null";
			return exceptionMessage;
		} else if (req.getMerchantId() == null) {

			exceptionMessage = "MerchantId is null";
			return exceptionMessage;
		} else if (req.getPosId() == null) {

			exceptionMessage = "PosId is null";
			return exceptionMessage;
		} else if (req.getSourceIp() == null) {

			exceptionMessage = "SourceIp is null";
			return exceptionMessage;
		}
		return exceptionMessage;
	}

	public void saveLocation(Transaction transaction) {

	}

	public Boolean checkLocationIfExsists(Integer merchantId) {

		Location location = locationRepository.findByMerchantId(merchantId);
		if (location == null) {
			logger.info("location does not esists");
			return false;
		}
		return true;
	}

	@Transactional
	public Boolean updateTransactionStatus(Integer transactionId, String status) {

		boolean isUpdated = false;
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
		Transaction transaction = transactionRepository.findByTransactionId(transactionId.longValue());

		if (transaction == null) {
			transaction = new Transaction();
			transaction.setStatus(TransactionStatus.FAILED.name());
		}

		try {
			if (transaction != null) {
				if (status.equalsIgnoreCase("Success")) {
					transaction.setStatus(TransactionStatus.COMPLETED.name());
				} else if (transaction.getStatus() == null || !transaction.getStatus().equalsIgnoreCase("COMPLETED")) {
					transaction.setStatus(TransactionStatus.PENDING.name());
				}
				transaction.setLastUpdated(dateFormat.format(new Date()));
				if (!transaction.getStatus().equalsIgnoreCase("failed")) {
					transactionRepository.save(transaction);
				}
				saveTransactionStatusLogs(transactionId, transaction.getStatus().toString(),
						transaction.getLastUpdated(), transaction.getSystemId());
				isUpdated = true;
			}
		} catch (Exception e) {
			logger.error("Transaction not found for ID: " + transactionId);
		}
		return isUpdated;
	}

	@Override
	@Transactional
	public TransactionStatusLogs saveTransactionStatusLogs(Integer transactionId, String status, String lastUpdated, String systemId) {
		TransactionStatusLogs transactionStatusLogs = null;

		try {
			transactionStatusLogs = new TransactionStatusLogs(transactionId, status, lastUpdated, systemId);
			transactionStatusLogsRepository.save(transactionStatusLogs);

		} catch (Exception ex) {
			logger.error("Exception occurred while saving TransactionStatusLogs, Error :" + ex.getMessage());
		}
		return transactionStatusLogs;
	}

	@Override
	public List<TransactionStatusLogs> getTransactionStatusLogs(int userId) {
		User user = userRepository.findById(userId).get();
		List<TransactionStatusLogs> transactionLog = transactionStatusLogsRepository.findTransactionStatusLogs(user.getSystemId());
		return transactionLog;
	}

	@Override
	public List<Location> getTopLocations(String username) {
		User user = userRepository.findByUserName(username);
		List<Location> locationList = new ArrayList<Location>();
		try {
			locationList = locationRepository.findTopLocations(user.getSystemId());
		} catch (Exception ex) {
            logger.error("Exception while getting Top Locations: "+ex.getMessage());
		}
		return locationList;
	}

	@Override
	public int countAllTransactionofSystem(String username) {
		int count = 0;
		try {
		User user = userRepository.findByUserName(username);
		 count = (int) transactionRepository.findTransactionCountofSystem(user.getSystemId());
		}catch(Exception ex) {
		  logger.error("Exception while counting transaction, Error : "+ex.getMessage());
		}
		return count;
	}
	

	public void saveLocationFromCSV() {
		List<Location> locationList = reader.saveLocations();
		locationRepository.saveAll(locationList);
	}
}
