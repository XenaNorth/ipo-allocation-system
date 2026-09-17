package com.example.ipoallocationsystem.config;

import com.example.ipoallocationsystem.entity.Ipo;
import com.example.ipoallocationsystem.repository.IpoRepository;
import com.example.ipoallocationsystem.repository.InvestorRepository;
import com.example.ipoallocationsystem.repository.SubscriptionOrderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.example.ipoallocationsystem.entity.Investor;
import com.example.ipoallocationsystem.entity.SubscriptionOrder;
import java.util.Random;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    private final IpoRepository ipoRepository;
    private final InvestorRepository investorRepository;
    private final SubscriptionOrderRepository orderRepository;

    public DataInitializer(IpoRepository ipoRepository, InvestorRepository investorRepository, SubscriptionOrderRepository orderRepository) {
        this.ipoRepository = ipoRepository;
        this.investorRepository = investorRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(String... args) {
        // If data already exists, skip - avoid re-generating every time the app restarts
        if (ipoRepository.count() > 0) {
            System.out.println("Data already exists, skipping initialization");
            return;
        }

        Ipo ipo = new Ipo();
        ipo.setCompanyName("NovaTech Inc.");
        ipo.setTotalShares(1_000_000);
        ipo.setPricePerShare(java.math.BigDecimal.valueOf(10));
        ipo.setMaxSharesPerInvestor(50_000);
        ipoRepository.save(ipo);

        System.out.println("IPO created: " + ipo.getCompanyName() + ", total shares: " + ipo.getTotalShares());

        Random random = new Random();
        long totalRequested = 0;

        for (int i = 0; i < 5000; i++) {
            Investor investor = new Investor();
            investor.setName("Investor" + i);
            investor.setAccountKey("acc_" + UUID.randomUUID());
            investorRepository.save(investor);

            int requested;
            if (random.nextDouble() < 0.05) {
                requested = ipo.getMaxSharesPerInvestor();  // 5% are big investors
            } else {
                requested = 100 + random.nextInt(2900);      // 100-3000 shares
            }

            SubscriptionOrder order = new SubscriptionOrder();
            order.setIpoId(ipo.getId());
            order.setInvestorId(investor.getId());
            order.setRequestedShares(requested);
            order.setSubmissionKey("sub_" + UUID.randomUUID());
            orderRepository.save(order);

            totalRequested += requested;

            // Print the first investor's account_key for later API testing
            if (i == 0) {
                System.out.println("First investor's account_key (for testing): " + investor.getAccountKey());
            }
        }

        double ratio = (double) totalRequested / ipo.getTotalShares();
        System.out.println("Total requested shares: " + totalRequested);
        System.out.printf("Oversubscription ratio: %.2f x%n", ratio);
    }
}
