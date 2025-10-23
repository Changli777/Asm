package poly.edu.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import poly.edu.entity.Category;
import poly.edu.entity.Product;
import poly.edu.service.CategoryService;
import poly.edu.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.support.RequestContextUtils;
import java.util.Map;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/product")
public class CRUDProductController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CRUDProductController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    // Thư mục lưu ảnh trong source (theo yêu cầu)
    private static final String PHOTOS_DIR = "src/main/resources/static/photos/";

    private static final DecimalFormat MONEY_FORMATTER = new DecimalFormat("#,###");

    /**
     * Trang danh sách + form
     * Nhận flash attributes "message" và "error" (nếu có) để hiển thị SweetAlert
     */

    @GetMapping
    public String index(Model model,
                        @RequestParam(value = "q", required = false) String q,
                        HttpServletRequest request) {

        // đọc flash attributes (nếu có) từ request — không expose biến rỗng vào model
        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        if (inputFlashMap != null) {
            if (inputFlashMap.containsKey("message")) {
                Object m = inputFlashMap.get("message");
                if (m != null) model.addAttribute("message", m.toString());
            }
            if (inputFlashMap.containsKey("error")) {
                Object e = inputFlashMap.get("error");
                if (e != null) model.addAttribute("error", e.toString());
            }
        }

        List<Product> products = productService.findAll();

        if (StringUtils.hasText(q)) {
            String qTrim = q.trim().toLowerCase();

            Optional<Product> exact = products.stream()
                    .filter(p -> p.getProductName() != null && p.getProductName().equalsIgnoreCase(qTrim))
                    .findFirst();

            if (exact.isPresent()) {
                Product p = exact.get();
                model.addAttribute("products", List.of(p));
                model.addAttribute("productForm", p);
                model.addAttribute("searchQuery", qTrim);
                model.addAttribute("formattedPriceMap", makeFormattedPriceMap(List.of(p)));
                model.addAttribute("categories", categoryService.findAll());
                return "admin/product";
            }

            List<Product> filtered = products.stream()
                    .filter(p -> (p.getProductName() != null && p.getProductName().toLowerCase().contains(qTrim))
                            || (p.getDescription() != null && p.getDescription().toLowerCase().contains(qTrim)))
                    .collect(Collectors.toList());

            model.addAttribute("products", filtered);
            model.addAttribute("productForm", (filtered.size() == 1) ? filtered.get(0) : new Product());
            model.addAttribute("searchQuery", qTrim);
            model.addAttribute("formattedPriceMap", makeFormattedPriceMap(filtered));
            model.addAttribute("categories", categoryService.findAll());

            if (!model.containsAttribute("message")) {
                if (filtered.isEmpty()) {
                    model.addAttribute("info", "Không tìm thấy product nào cho: " + qTrim);

                } else if (filtered.size() > 1) {
                    // mới: sử dụng "info" cho thông báo tìm kiếm (không phải flash success)
                    model.addAttribute("info", "Tìm được " + filtered.size() + " kết quả. Chọn Edit để load vào form.");
                }
            }

            return "admin/product";
        }

        // no query
        model.addAttribute("products", products);
        model.addAttribute("productForm", new Product());
        model.addAttribute("searchQuery", null);
        model.addAttribute("formattedPriceMap", makeFormattedPriceMap(products));
        model.addAttribute("categories", categoryService.findAll());

        return "admin/product";
    }


    /**
     * Hiển thị form create hoặc edit
     * Cho phép cả query param ?id=... và path /create/{id}
     */
    @GetMapping({"/create", "/create/{id}"})
    public String createForm(@PathVariable(value = "id", required = false) Long idPath,
                             @RequestParam(value = "id", required = false) Long idParam,
                             Model model) {

        Long id = (idPath != null) ? idPath : idParam; // ưu tiên path-var

        if (id != null) {
            try {
                Product p = productService.findById(id);
                model.addAttribute("productForm", p);
            } catch (Exception ex) {
                logger.error("Không tìm product id=" + id, ex);
                model.addAttribute("productForm", new Product());
                model.addAttribute("error", "Không tìm thấy product với id = " + id);
            }
        } else {
            model.addAttribute("productForm", new Product());
        }

        List<Product> all = productService.findAll();
        model.addAttribute("products", all);
        model.addAttribute("formattedPriceMap", makeFormattedPriceMap(all));
        model.addAttribute("categories", categoryService.findAll());
        return "admin/product";
    }

    /**
     * Lưu product (create hoặc update)
     * POST /admin/product/save/{id}
     */
    @PostMapping("/save/{id}")
    public String save(@PathVariable("id") Long id,
                       @ModelAttribute("productForm") Product form,
                       @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                       Model model,
                       RedirectAttributes redirectAttrs) {

        // validation cơ bản
        if (!StringUtils.hasText(form.getProductName())) {
            model.addAttribute("error", "Chưa nhập tên sản phẩm");
            model.addAttribute("products", productService.findAll());
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("formattedPriceMap", makeFormattedPriceMap(productService.findAll()));
            return "admin/product";
        }
        if (form.getPrice() == null || form.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            model.addAttribute("error", "Chưa nhập giá sản phẩm hợp lệ");
            model.addAttribute("products", productService.findAll());
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("formattedPriceMap", makeFormattedPriceMap(productService.findAll()));
            return "admin/product";
        }
        if (form.getStockQuantity() == null || form.getStockQuantity() < 1) {
            model.addAttribute("error", "Chưa nhập số lượng hợp lệ");
            model.addAttribute("products", productService.findAll());
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("formattedPriceMap", makeFormattedPriceMap(productService.findAll()));
            return "admin/product";
        }
        if (form.getCategory() == null || form.getCategory().getCategoryId() == null) {
            model.addAttribute("error", "Chưa chọn category");
            model.addAttribute("products", productService.findAll());
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("formattedPriceMap", makeFormattedPriceMap(productService.findAll()));
            return "admin/product";
        }

        // đính category đầy đủ từ DB
        Long catId = form.getCategory().getCategoryId();
        Category cat = categoryService.findById(catId);
        form.setCategory(cat);

        try {
            // xử lý upload file nếu có
            if (imageFile != null && !imageFile.isEmpty()) {
                String original = imageFile.getOriginalFilename();
                String ext = "";
                if (original != null && original.contains(".")) {
                    ext = original.substring(original.lastIndexOf("."));
                }
                String filename = UUID.randomUUID().toString() + ext;

                Path dirPath = Paths.get(PHOTOS_DIR);
                if (!Files.exists(dirPath)) {
                    Files.createDirectories(dirPath);
                }
                Path filePath = dirPath.resolve(filename);
                imageFile.transferTo(filePath.toFile());
                form.setImageUrl("/photos/" + filename);
            }

            if (id != null && id > 0) {
                // UPDATE
                Product exist = productService.findById(id);
                exist.setProductName(form.getProductName());
                exist.setDescription(form.getDescription());
                exist.setPrice(form.getPrice());
                exist.setDiscountPrice(form.getDiscountPrice());
                exist.setStockQuantity(form.getStockQuantity());
                exist.setCategory(form.getCategory());
                exist.setIsFeatured(form.getIsFeatured());
                exist.setIsNew(form.getIsNew());
                exist.setIsOnSale(form.getIsOnSale());
                if (StringUtils.hasText(form.getImageUrl())) {
                    exist.setImageUrl(form.getImageUrl());
                }
                if (form.getViewsCount() != null) exist.setViewsCount(form.getViewsCount());
                if (form.getSoldCount() != null) exist.setSoldCount(form.getSoldCount());
                productService.update(exist);
                redirectAttrs.addFlashAttribute("message", "Cập nhật product thành công.");
            } else {
                // CREATE
                if (form.getViewsCount() == null) form.setViewsCount(0);
                if (form.getSoldCount() == null) form.setSoldCount(0);
                form.setCreatedAt(LocalDateTime.now());
                form.setUpdatedAt(LocalDateTime.now());
                productService.create(form);
                redirectAttrs.addFlashAttribute("message", "Tạo product mới thành công.");
            }

            return "redirect:/admin/product";
        } catch (Exception ex) {
            logger.error("Lỗi khi lưu product", ex);
            model.addAttribute("error", "Lỗi khi lưu product: " + ex.getMessage());
            model.addAttribute("products", productService.findAll());
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("formattedPriceMap", makeFormattedPriceMap(productService.findAll()));
            model.addAttribute("productForm", form);
            return "admin/product";
        }
    }

    /**
     * Xóa product bằng POST
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttrs) {
        try {
            productService.deleteById(id);
            redirectAttrs.addFlashAttribute("message", "Xóa product thành công.");
            return "redirect:/admin/product";
        } catch (Exception ex) {
            logger.error("Lỗi khi xóa product id=" + id, ex);
            model.addAttribute("error", "Lỗi khi xóa: " + ex.getMessage());
            model.addAttribute("products", productService.findAll());
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("formattedPriceMap", makeFormattedPriceMap(productService.findAll()));
            model.addAttribute("productForm", new Product());
            return "admin/product";
        }
    }

    // helper: tạo map productId -> { price, discount, final } (định dạng tiền)
    private java.util.Map<Long, java.util.Map<String, String>> makeFormattedPriceMap(List<Product> products) {
        java.util.Map<Long, java.util.Map<String, String>> map = new java.util.HashMap<>();
        for (Product p : products) {
            java.util.Map<String, String> m = new java.util.HashMap<>();
            try {
                String price = (p.getPrice() != null) ? MONEY_FORMATTER.format(p.getPrice()) : "";
                String discount = p.getDiscountPrice() != null ? MONEY_FORMATTER.format(p.getDiscountPrice()) : "";
                BigDecimal finalPrice = p.getFinalPrice();
                String finalS = finalPrice != null ? MONEY_FORMATTER.format(finalPrice) : "";
                m.put("price", price);
                m.put("discount", discount);
                m.put("final", finalS);
            } catch (Exception ex) {
                m.put("price", p.getPrice() != null ? p.getPrice().toString() : "");
                m.put("discount", p.getDiscountPrice() != null ? p.getDiscountPrice().toString() : "");
                m.put("final", p.getFinalPrice() != null ? p.getFinalPrice().toString() : "");
            }
            // watch out for null productId (new transient product): we guard by skipping null id
            if (p.getProductId() != null) {
                map.put(p.getProductId(), m);
            }
        }
        return map;
    }
}
