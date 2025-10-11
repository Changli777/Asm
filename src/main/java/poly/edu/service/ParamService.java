package poly.edu.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class ParamService {

    // Spring sẽ inject request hiện tại
    @jakarta.annotation.Resource
    private HttpServletRequest request;

    public String getString(String name, String defaultValue) {
        String val = request.getParameter(name);
        if (val == null) return defaultValue;
        val = val.trim();
        return val.isEmpty() ? defaultValue : val;
    }

    public int getInt(String name, int defaultValue) {
        String val = request.getParameter(name);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public double getDouble(String name, double defaultValue) {
        String val = request.getParameter(name);
        if (val == null) return defaultValue;
        try {
            return Double.parseDouble(val.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        String val = request.getParameter(name);
        if (val == null) return defaultValue;
        val = val.trim().toLowerCase();
        // hiểu nhiều dạng: "true","1","on","yes"
        if ("true".equals(val) || "1".equals(val) || "on".equals(val) || "yes".equals(val)) return true;
        if ("false".equals(val) || "0".equals(val) || "off".equals(val) || "no".equals(val)) return false;
        return defaultValue;
    }

    public Date getDate(String name, String pattern) {
        String val = request.getParameter(name);
        if (val == null || val.trim().isEmpty()) return null;
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setLenient(false);
        try {
            return sdf.parse(val.trim());
        } catch (ParseException ex) {
            throw new RuntimeException("Invalid date format for parameter '" + name + "': expected " + pattern, ex);
        }
    }

    /**
     * Lưu file upload vào thư mục (tính từ webroot/application context)
     * @param file MultipartFile từ form
     * @param path đường dẫn tương đối từ webroot, ví dụ "/uploads" hoặc "uploads"
     * @return File đã lưu (absolute) hoặc null nếu không có file
     */
    public File save(MultipartFile file, String path) {
        if (file == null || file.isEmpty()) return null;

        // đảm bảo path có dạng bắt đầu hoặc không; dùng servlet context để quy về đường dẫn tuyệt đối
        String ctxPath = request.getServletContext().getRealPath("/");
        if (ctxPath == null) {
            // fallback: dùng user.dir
            ctxPath = System.getProperty("user.dir") + File.separator;
        }

        // chuẩn hóa path
        String rel = path == null ? "uploads" : path;
        if (rel.startsWith("/")) rel = rel.substring(1);

        Path uploadDir = Paths.get(ctxPath, rel);
        try {
            Files.createDirectories(uploadDir);
            String original = file.getOriginalFilename();
            String filename = System.currentTimeMillis() + (original == null ? "" : "_" + original.replaceAll("\\s+", "_"));
            Path dest = uploadDir.resolve(filename);
            file.transferTo(dest.toFile());
            return dest.toFile();
        } catch (IOException ex) {
            throw new RuntimeException("Không thể lưu file: " + ex.getMessage(), ex);
        }
    }
}
