package poly.edu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import poly.edu.dao.CategoryDAO;
import poly.edu.dao.ProductDAO;
import poly.edu.entity.Category;
import poly.edu.entity.Product;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    private static final int PAGE_SIZE = 4;

    @Autowired
    private CategoryDAO categoryDAO;

    @Autowired
    private ProductDAO productDAO;

    // Link: http://localhost:8080/home

    @GetMapping("/home")
    public String home(
            @RequestParam(value = "newPage", required = false) Integer newPageParam,
            @RequestParam(value = "salePage", required = false) Integer salePageParam,
            @RequestParam(value = "hotPage", required = false) Integer hotPageParam,
            Model model
    ) {
        try {
            // 1. load categories
            List<Category> categories = categoryDAO.findAll();
            model.addAttribute("categories", categories);

            // 2. fetch full lists (you confirmed ProductDAO has no-arg methods)
            List<Product> allNew = productDAO.findNewProducts();
            List<Product> allSale = productDAO.findSaleProducts();
            List<Product> allHot = productDAO.findFeaturedProducts();

            // 3. ensure page params default to 1
            int newPage = (newPageParam == null || newPageParam < 1) ? 1 : newPageParam;
            int salePage = (salePageParam == null || salePageParam < 1) ? 1 : salePageParam;
            int hotPage = (hotPageParam == null || hotPageParam < 1) ? 1 : hotPageParam;

            // 4. compute paged (circular fill) slices
            List<Product> newPageItems = circularPage(allNew, newPage, PAGE_SIZE);
            List<Product> salePageItems = circularPage(allSale, salePage, PAGE_SIZE);
            List<Product> hotPageItems = circularPage(allHot, hotPage, PAGE_SIZE);

            // 5. set category name for display (keeps your existing approach)
            for (Product p : newPageItems) {
                setCategoryNameIfPresent(p);
            }
            for (Product p : salePageItems) {
                setCategoryNameIfPresent(p);
            }
            for (Product p : hotPageItems) {
                setCategoryNameIfPresent(p);
            }

            // 6. prepare prev/next urls for each row, preserving other rows' page params
            int totalNewPages = computeTotalPages(allNew.size(), PAGE_SIZE);
            int totalSalePages = computeTotalPages(allSale.size(), PAGE_SIZE);
            int totalHotPages = computeTotalPages(allHot.size(), PAGE_SIZE);

            // normalize requested pages into [1..totalPages] (if totalPages==0 treat as 1)
            newPage = normalizePage(newPage, totalNewPages);
            salePage = normalizePage(salePage, totalSalePages);
            hotPage = normalizePage(hotPage, totalHotPages);

            String newPrevUrl  = buildUrl("newPage",  prevOf(newPage, totalNewPages), "salePage", salePage, "hotPage", hotPage);
            String newNextUrl  = buildUrl("newPage",  nextOf(newPage, totalNewPages), "salePage", salePage, "hotPage", hotPage);

            String salePrevUrl = buildUrl("newPage", newPage, "salePage", prevOf(salePage, totalSalePages), "hotPage", hotPage);
            String saleNextUrl = buildUrl("newPage", newPage, "salePage", nextOf(salePage, totalSalePages), "hotPage", hotPage);

            String hotPrevUrl  = buildUrl("newPage", newPage, "salePage", salePage, "hotPage", prevOf(hotPage, totalHotPages));
            String hotNextUrl  = buildUrl("newPage", newPage, "salePage", salePage, "hotPage", nextOf(hotPage, totalHotPages));

            // 7. put into model
            model.addAttribute("newProductsPage", newPageItems);
            model.addAttribute("saleProductsPage", salePageItems);
            model.addAttribute("hotProductsPage", hotPageItems);

            model.addAttribute("newPrevUrl", newPrevUrl);
            model.addAttribute("newNextUrl", newNextUrl);
            model.addAttribute("salePrevUrl", salePrevUrl);
            model.addAttribute("saleNextUrl", saleNextUrl);
            model.addAttribute("hotPrevUrl", hotPrevUrl);
            model.addAttribute("hotNextUrl", hotNextUrl);

            // also add current page numbers and totals optionally (for debugging or UI)
            model.addAttribute("newPageCurrent", newPage);
            model.addAttribute("salePageCurrent", salePage);
            model.addAttribute("hotPageCurrent", hotPage);

            model.addAttribute("newTotalPages", totalNewPages);
            model.addAttribute("saleTotalPages", totalSalePages);
            model.addAttribute("hotTotalPages", totalHotPages);

            return "home";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    // ---------- helper methods ----------

    private List<Product> circularPage(List<Product> all, int requestedPage, int pageSize) {
        List<Product> result = new ArrayList<>();
        if (all == null || all.isEmpty()) {
            return result;
        }

        int total = all.size();
        int totalPages = computeTotalPages(total, pageSize);
        int page = normalizePage(requestedPage, totalPages); // ensure 1..totalPages

        int startIndex = (page - 1) * pageSize; // 0-based
        for (int i = 0; i < pageSize; i++) {
            int idx = (startIndex + i) % total; // circular index
            result.add(all.get(idx));
        }
        return result;
    }

    private int computeTotalPages(int totalItems, int pageSize) {
        if (totalItems <= 0) return 1; // treat empty as 1 page to simplify wrap logic
        return (totalItems + pageSize - 1) / pageSize;
    }

    private int normalizePage(int p, int totalPages) {
        if (totalPages <= 0) return 1;
        int mod = ((p - 1) % totalPages + totalPages) % totalPages; // 0..totalPages-1
        return mod + 1;
    }

    private int prevOf(int current, int totalPages) {
        if (totalPages <= 1) return 1;
        return current == 1 ? totalPages : (current - 1);
    }

    private int nextOf(int current, int totalPages) {
        if (totalPages <= 1) return 1;
        return current == totalPages ? 1 : (current + 1);
    }

    private String buildUrl(String k1, int v1, String k2, int v2, String k3, int v3) {
        // keep simple: /home?k1=v1&k2=v2&k3=v3
        return String.format("/home?%s=%d&%s=%d&%s=%d", k1, v1, k2, v2, k3, v3);
    }

    private void setCategoryNameIfPresent(Product product) {
        if (product == null) return;
        if (product.getCategoryId() != null) {
            categoryDAO.findById(product.getCategoryId()).ifPresent(category -> {
                product.setCategoryName(category.getCategoryName());
            });
        }
    }
}
