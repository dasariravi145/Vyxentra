package com.vyxentra.vehicle.config;


import com.vyxentra.vehicle.gateway.PayUGateway;
import com.vyxentra.vehicle.gateway.PaymentGatewayFactory;
import com.vyxentra.vehicle.gateway.RazorpayGateway;
import com.vyxentra.vehicle.gateway.StripeGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class PaymentGatewayConfig {

    @Value("${payment.gateway.default:RAZORPAY}")
    private String defaultGateway;

    @Bean
    @Primary
    public PaymentGatewayFactory paymentGatewayFactory(
            RazorpayGateway razorpayGateway,
            StripeGateway stripeGateway,
            PayUGateway payUGateway) {

        // Create factory with constructor injection
        PaymentGatewayFactory factory = new PaymentGatewayFactory(
                razorpayGateway,
                stripeGateway,
                payUGateway
        );

        // Set default gateway (optional as it's already set via @Value)
        factory.setDefaultGateway(defaultGateway);

        return factory;
    }
}
