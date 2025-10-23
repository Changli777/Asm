package poly.edu.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.config.VNPayConfig;
import poly.edu.entity.Order;
import poly.edu.service.OrderService;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/checkout/vnpay")
public class VNPayController {

    @Autowired
    VNPayConfig config;

    @Autowired
    OrderService orderService;

    @PostMapping("/confirm")
    public String confirmOrder(
            HttpServletRequest req,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam(value = "cardNumber", required = false) String cardNumber,
            @RequestParam(value = "expiry", required = false) String expiry,
            @RequestParam(value = "cvv", required = false) String cvv,
            @RequestParam(value = "total", required = false, defaultValue = "100000") BigDecimal total,
            Model model
    ) throws Exception {

        if (paymentMethod.equals("COD")) {
            model.addAttribute("message", "Đặt hàng thành công! Thanh toán khi nhận hàng.");
            return "home";
        }

        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            model.addAttribute("error", "Vui lòng nhập số thẻ hoặc tài khoản ví!");
            return "checkout";
        }

        if (!"9704198526191432198".equals(cardNumber)) {
            model.addAttribute("error", "Số thẻ không hợp lệ hoặc không được hỗ trợ!");
            return "checkout";
        }

        String orderType = "other";
        long amount = total.multiply(BigDecimal.valueOf(100)).longValue();
        String vnp_TxnRef = String.valueOf(System.currentTimeMillis());
        String vnp_IpAddr = req.getRemoteAddr();

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", config.vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toán đơn hàng: " + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", config.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_BankCode", "NCB");

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (Iterator<String> it = fieldNames.iterator(); it.hasNext();) {
            String fieldName = it.next();
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII)).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (it.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String secureHash = hmacSHA512(config.vnp_HashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);
        String paymentUrl = config.vnp_Url + "?" + query.toString();

        return "redirect:" + paymentUrl;
    }

    @GetMapping("/return")
    public String vnpayReturn(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Map<String, String> params = new HashMap<>();
        for (Enumeration<String> en = request.getParameterNames(); en.hasMoreElements();) {
            String paramName = en.nextElement();
            String paramValue = request.getParameter(paramName);
            params.put(paramName, paramValue);
        }

        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef");

        if ("00".equals(responseCode)) {
            orderService.findByOrderNumber(txnRef).ifPresent(order -> {
                order.setPaymentStatus("Đã thanh toán");
                order.setStatus("Đã thanh toán");
                order.setPaymentTransactionId(txnRef);
                orderService.save(order);
            });
            redirectAttributes.addFlashAttribute("message", "Thanh toán thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Thanh toán thất bại, vui lòng thử lại!");
        }

        return "redirect:/home";
    }

    public static String hmacSHA512(String key, String data) throws Exception {
        javax.crypto.Mac hmac512 = javax.crypto.Mac.getInstance("HmacSHA512");
        javax.crypto.spec.SecretKeySpec secret_key =
                new javax.crypto.spec.SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmac512.init(secret_key);
        byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hash = new StringBuilder();
        for (byte b : bytes) hash.append(String.format("%02x", b));
        return hash.toString();
    }
}
