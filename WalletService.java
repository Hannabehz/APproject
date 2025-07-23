package service;


import DAO.OrderDAO;
import DAO.TransactionDAO;
import DAO.UserDAO;
import DAO.UserWalletDAO;
import entity.Order;
import entity.Transaction;
import entity.User;
import entity.UserWallet;
import org.hibernate.Session;
import util.HibernateUtil;

import java.time.LocalDateTime;
import java.util.*;

public class WalletService {
    private final UserDAO userDAO;
    private final UserWalletDAO walletDAO;
    private final TransactionDAO transactionDAO;
    private final OrderDAO orderDAO;

    public WalletService() {
        this.userDAO = new UserDAO();
        this.walletDAO = new UserWalletDAO();
        this.transactionDAO = new TransactionDAO();
        this.orderDAO = new OrderDAO();
    }

    // شبیه‌سازی ادغام با درگاه پرداخت
    private boolean processPaymentGateway(String method, double amount, String bankName, String accountNumber) {
        // فرض: درگاه پرداخت همیشه موفق است
        // در عمل، باید با API درگاه (مثل زرین‌پال) تماس بگیرید
        System.out.println("Processing payment via " + method + " for amount: " + amount);
        System.out.println("Bank details: " + bankName + ", Account Number: " + accountNumber);
        return true; // شبیه‌سازی موفقیت
    }

    // POST /wallet/top-up
    public Map<String, Object> topUpWallet(String token, String method, double amount, String bankName, String accountNumber) {
        if (method == null || !List.of("online", "card").contains(method)) {
            return new HashMap<>(Map.of("error", "Invalid `method`", "status", 400));
        }
        if (amount <= 0) {
            return new HashMap<>(Map.of("error", "Invalid `amount`", "status", 400));
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            User user = userDAO.findUserByToken(token);
            if (user == null) {
                return new HashMap<>(Map.of("error", "Unauthorized", "status", 401));
            }

            // پردازش درگاه پرداخت (برای روش‌های online یا card)
            if ("online".equals(method) || "card".equals(method)) {
                boolean paymentSuccessful = processPaymentGateway(method, amount, bankName, accountNumber);
                if (!paymentSuccessful) {
                    return new HashMap<>(Map.of("error", "Payment gateway failed", "status", 400));
                }
            }

            Optional<UserWallet> walletOptional = walletDAO.findByUserId(user.getId());
            UserWallet wallet;
            if (walletOptional.isPresent()) {
                wallet = walletOptional.get();
            } else {
                // ایجاد کیف پول جدید
                wallet = new UserWallet();
                wallet.setUser(user);
                wallet.setBalance(0.0);
            }

            wallet.setBalance(wallet.getBalance() + amount);
            walletDAO.saveOrUpdate(wallet);

            Transaction transaction = new Transaction();
            transaction.setUser(user);
            transaction.setAmount(amount);
            transaction.setType("top-up");
            transaction.setMethod(method);
            transaction.setStatus("successful");
            transaction.setCreatedAt(LocalDateTime.now());
            transactionDAO.saveTransaction(transaction);

            session.getTransaction().commit();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Wallet topped up successfully");
            response.put("new_balance", wallet.getBalance());
            response.put("status", 200);
            return response;
        } catch (Exception e) {
            return new HashMap<>(Map.of("error", e.getMessage(), "status", 500));
        }
    }

    // POST /payment/online
    public Map<String, Object> makeOnlinePayment(String token, String orderId, String method, String bankName, String accountNumber) {
        if (orderId == null || orderId.trim().isEmpty()) {
            return new HashMap<>(Map.of("error", "Invalid `orderId`", "status", 400));
        }
        if (method == null || !List.of("wallet", "paywall").contains(method)) {
            return new HashMap<>(Map.of("error", "Invalid `method`", "status", 400));
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            User user = userDAO.findUserByToken(token);
            if (user == null) {
                return new HashMap<>(Map.of("error", "Unauthorized", "status", 401));
            }

            UUID orderUuid;
            try {
                orderUuid = UUID.fromString(orderId);
            } catch (IllegalArgumentException e) {
                return new HashMap<>(Map.of("error", "Invalid `orderId` format", "status", 400));
            }

            Optional<Order> orderOptional = orderDAO.findById(orderUuid);
            if (orderOptional.isEmpty()) {
                return new HashMap<>(Map.of("error", "Order not found", "status", 404));
            }
            Order order = orderOptional.get();


            if ("wallet".equals(method)) {
                Optional<UserWallet> walletOptional = walletDAO.findByUserId(user.getId());
                if (walletOptional.isEmpty()) {
                    return new HashMap<>(Map.of("error", "Wallet not found", "status", 400));
                }
                UserWallet wallet = walletOptional.get();

                if (wallet.getBalance() < order.getPayPrice()) {
                    return new HashMap<>(Map.of("error", "Insufficient balance", "status", 400));
                }

                wallet.setBalance(wallet.getBalance() - order.getPayPrice());
                walletDAO.saveOrUpdate(wallet);
            } else if ("paywall".equals(method)) {
                boolean paymentSuccessful = processPaymentGateway(method, order.getPayPrice(), bankName, accountNumber);
                if (!paymentSuccessful) {
                    return new HashMap<>(Map.of("error", "Payment gateway failed", "status", 400));
                }
            }

            order.setStatus("submitted");
            order.setUpdatedAt(LocalDateTime.now());
            orderDAO.saveOrder(order);

            Transaction transaction = new Transaction();
            transaction.setUser(user);
            transaction.setOrder(order);
            transaction.setAmount(order.getPayPrice());
            transaction.setType("payment");
            transaction.setMethod(method);
            transaction.setStatus("successful");
            transaction.setCreatedAt(LocalDateTime.now());
            transactionDAO.saveTransaction(transaction);

            session.getTransaction().commit();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Payment successful");
            response.put("order_id", order.getId().toString());
            response.put("status", 200);
            return response;
        } catch (Exception e) {
            return new HashMap<>(Map.of("error", e.getMessage(), "status", 500));
        }
    }
    public Map<String, Object> getWalletBalance(String token) {
        if (token == null || token.trim().isEmpty()) {
            return new HashMap<>(Map.of("error", "Invalid `token`", "status", 400));
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = userDAO.findUserByToken(token);
            if (user == null) {
                return new HashMap<>(Map.of("error", "Unauthorized", "status", 401));
            }

            Optional<UserWallet> walletOptional = walletDAO.findByUserId(user.getId());
            if (walletOptional.isEmpty()) {
                return new HashMap<>(Map.of("error", "Wallet not found", "status", 404));
            }

            UserWallet wallet = walletOptional.get();
            Map<String, Object> response = new HashMap<>();
            response.put("status", 200);
            response.put("message", "Wallet balance retrieved successfully");
            response.put("balance", wallet.getBalance());
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>(Map.of("error", "Failed to retrieve wallet balance: " + e.getMessage(), "status", 500));
        }
    }
}