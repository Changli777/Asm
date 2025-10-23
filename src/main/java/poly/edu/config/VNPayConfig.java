package poly.edu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration

public class VNPayConfig {
    @Value("${vnpay.tmnCode}")
    public String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    public String vnp_HashSecret;

    @Value("${vnpay.url}")
    public String vnp_Url;

    @Value("${vnpay.returnUrl}")
    public String vnp_ReturnUrl;

    @Value("${vnpay.ipnUrl}")
    public String vnp_IpnUrl;

    @Value("${vnpay.version}")
    public String vnp_Version;

    @Value("${vnpay.command}")
    public String vnp_Command;

    @Value("${vnpay.locale}")
    public String vnp_Locale;
}
