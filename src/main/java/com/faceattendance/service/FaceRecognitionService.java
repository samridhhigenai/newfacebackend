package com.faceattendance.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import javax.imageio.ImageIO;

@Service
public class FaceRecognitionService {

    @Value("${face.recognition.confidence.threshold:80.0}")
    private double confidenceThreshold;

    @Value("${face.recognition.model.path:models/}")
    private String modelPath;

    @PostConstruct
    public void init() {
        try {
            // Create models directory if it doesn't exist
            Files.createDirectories(Paths.get(modelPath));
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize face recognition service", e);
        }
    }

    /**
     * Enhanced face image validation with quality checks
     */
    public boolean validateFaceImage(String base64Image) {
        try {
            // Handle different base64 formats
            String cleanBase64 = base64Image;
            if (base64Image.contains(",")) {
                // Remove data URL prefix if present (e.g., "data:image/jpeg;base64,")
                cleanBase64 = base64Image.split(",")[1];
            }

            byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));

            // Basic validation: check image dimensions and format
            if (image == null) {
                System.out.println("Image validation failed: Image is null after decoding");
                return false;
            }

            // Check minimum dimensions (face should be at least 100x100 pixels for good quality)
            if (image.getWidth() < 100 || image.getHeight() < 100) {
                System.out.println("Image validation failed: Image too small: " + image.getWidth() + "x" + image.getHeight() + " (minimum 100x100)");
                return false;
            }

            // Check maximum dimensions (prevent extremely large images)
            if (image.getWidth() > 2000 || image.getHeight() > 2000) {
                System.out.println("Image validation failed: Image too large: " + image.getWidth() + "x" + image.getHeight() + " (maximum 2000x2000)");
                return false;
            }

            // Check image quality metrics
            if (!checkImageQuality(image)) {
                System.out.println("Image validation failed: Poor image quality detected");
                return false;
            }

            System.out.println("Image validation passed: " + image.getWidth() + "x" + image.getHeight() + " with good quality");
            return true;
        } catch (Exception e) {
            System.out.println("Image validation failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check image quality for face recognition
     */
    private boolean checkImageQuality(BufferedImage image) {
        try {
            int width = image.getWidth();
            int height = image.getHeight();

            // Convert to grayscale for quality analysis
            int[] grayPixels = convertToGrayscale(image);

            // Check for sufficient contrast
            int minPixel = 255, maxPixel = 0;
            double sum = 0;
            for (int pixel : grayPixels) {
                minPixel = Math.min(minPixel, pixel);
                maxPixel = Math.max(maxPixel, pixel);
                sum += pixel;
            }

            int contrast = maxPixel - minPixel;
            double avgBrightness = sum / grayPixels.length;

            // Require minimum contrast (at least 30 levels difference)
            if (contrast < 30) {
                System.out.println("Quality check failed: Low contrast (" + contrast + " < 30)");
                return false;
            }

            // Check for reasonable brightness (not too dark or too bright)
            if (avgBrightness < 30 || avgBrightness > 225) {
                System.out.println("Quality check failed: Poor brightness (" + String.format("%.1f", avgBrightness) + " not in range 30-225)");
                return false;
            }

            // Check for image sharpness using edge detection
            double sharpness = calculateSharpness(grayPixels, width, height);
            if (sharpness < 5.0) {
                System.out.println("Quality check failed: Image too blurry (sharpness: " + String.format("%.2f", sharpness) + " < 5.0)");
                return false;
            }

            System.out.println("Quality metrics - Contrast: " + contrast + ", Brightness: " + String.format("%.1f", avgBrightness) + ", Sharpness: " + String.format("%.2f", sharpness));
            return true;

        } catch (Exception e) {
            System.out.println("Quality check error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Calculate image sharpness using gradient magnitude
     */
    private double calculateSharpness(int[] grayPixels, int width, int height) {
        double totalGradient = 0;
        int count = 0;

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int idx = y * width + x;

                // Calculate gradients
                int gx = grayPixels[idx + 1] - grayPixels[idx - 1];
                int gy = grayPixels[idx + width] - grayPixels[idx - width];

                // Calculate gradient magnitude
                double magnitude = Math.sqrt(gx * gx + gy * gy);
                totalGradient += magnitude;
                count++;
            }
        }

        return count > 0 ? totalGradient / count : 0;
    }

    /**
     * Extract face encoding from image (simplified approach)
     */
    public String extractFaceEncoding(String base64Image) {
        try {
            // Validate the image first
            if (!validateFaceImage(base64Image)) {
                throw new RuntimeException("Invalid face image provided");
            }

            // Handle different base64 formats
            String cleanBase64 = base64Image;
            if (base64Image.contains(",")) {
                // Remove data URL prefix if present (e.g., "data:image/jpeg;base64,")
                cleanBase64 = base64Image.split(",")[1];
            }

            // Decode base64 image
            byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));

            if (image == null) {
                throw new RuntimeException("Failed to decode image from base64");
            }

            // Create a simplified face encoding using image features
            // In a production system, you would use proper face recognition algorithms
            String encoding = createSimpleFaceEncoding(image);

            System.out.println("Face encoding created successfully");
            return encoding;

        } catch (Exception e) {
            System.out.println("Failed to extract face encoding: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to extract face encoding: " + e.getMessage(), e);
        }
    }

    /**
     * Create a more robust face encoding using multiple image features
     */
    private String createSimpleFaceEncoding(BufferedImage image) {
        try {
            // STANDARDIZED image size for ALL employees - no exceptions
            final int STANDARD_WIDTH = 256;
            final int STANDARD_HEIGHT = 256;
            BufferedImage resizedImage = resizeImage(image, STANDARD_WIDTH, STANDARD_HEIGHT);

            // Convert to grayscale and extract multiple features
            int[] grayPixels = convertToGrayscale(resizedImage);

            // Extract multiple feature types for better uniqueness - STANDARDIZED for all employees
            int[] histogram = createHistogram(grayPixels);
            double[] lbpFeatures = extractLBPFeatures(resizedImage);
            double[] edgeFeatures = extractEdgeFeatures(grayPixels, STANDARD_WIDTH, STANDARD_HEIGHT);
            double[] textureFeatures = extractTextureFeatures(grayPixels, STANDARD_WIDTH, STANDARD_HEIGHT);

            // Combine all features into a comprehensive encoding
            StringBuilder encoding = new StringBuilder();

            // Add histogram features (normalized)
            encoding.append("HIST:");
            for (int value : histogram) {
                encoding.append(value).append(",");
            }

            // Add LBP features
            encoding.append("LBP:");
            for (double value : lbpFeatures) {
                encoding.append(String.format("%.3f", value)).append(",");
            }

            // Add edge features
            encoding.append("EDGE:");
            for (double value : edgeFeatures) {
                encoding.append(String.format("%.3f", value)).append(",");
            }

            // Add texture features
            encoding.append("TEXT:");
            for (double value : textureFeatures) {
                encoding.append(String.format("%.3f", value)).append(",");
            }

            // Create a unique hash for quick comparison - SAME LOGIC FOR ALL EMPLOYEES
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(encoding.toString().getBytes());
            String hash = Base64.getEncoder().encodeToString(hashBytes).substring(0, 16);

            System.out.println("STANDARDIZED encoding created for employee (same logic for all): " +
                             STANDARD_WIDTH + "x" + STANDARD_HEIGHT + " format");

            return hash + ":" + encoding.toString();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create face encoding", e);
        }
    }

    /**
     * Resize image to specified dimensions
     */
    private BufferedImage resizeImage(BufferedImage original, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        resized.getGraphics().drawImage(original.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH), 0, 0, null);
        return resized;
    }

    /**
     * Convert image to grayscale
     */
    private int[] convertToGrayscale(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] grayPixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Convert to grayscale using luminance formula
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                grayPixels[y * width + x] = gray;
            }
        }

        return grayPixels;
    }

    /**
     * Create histogram of grayscale values
     */
    private int[] createHistogram(int[] grayPixels) {
        int[] histogram = new int[256];

        for (int pixel : grayPixels) {
            histogram[pixel]++;
        }

        return histogram;
    }

    /**
     * Extract Local Binary Pattern (LBP) features for better face characterization
     */
    private double[] extractLBPFeatures(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] lbpHistogram = new int[256];

        // Convert to grayscale array for easier processing
        int[][] grayImage = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                grayImage[y][x] = (int) (0.299 * r + 0.587 * g + 0.114 * b);
            }
        }

        // Calculate LBP for each pixel (excluding borders)
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int center = grayImage[y][x];
                int lbpValue = 0;

                // Check 8 neighbors in clockwise order
                if (grayImage[y-1][x-1] >= center) lbpValue |= 1;
                if (grayImage[y-1][x] >= center) lbpValue |= 2;
                if (grayImage[y-1][x+1] >= center) lbpValue |= 4;
                if (grayImage[y][x+1] >= center) lbpValue |= 8;
                if (grayImage[y+1][x+1] >= center) lbpValue |= 16;
                if (grayImage[y+1][x] >= center) lbpValue |= 32;
                if (grayImage[y+1][x-1] >= center) lbpValue |= 64;
                if (grayImage[y][x-1] >= center) lbpValue |= 128;

                lbpHistogram[lbpValue]++;
            }
        }

        // Normalize histogram
        double[] normalizedLBP = new double[256];
        int totalPixels = (width - 2) * (height - 2);
        for (int i = 0; i < 256; i++) {
            normalizedLBP[i] = (double) lbpHistogram[i] / totalPixels;
        }

        return normalizedLBP;
    }

    /**
     * Extract edge features using simple gradient calculation
     */
    private double[] extractEdgeFeatures(int[] grayPixels, int width, int height) {
        double[] edgeFeatures = new double[8]; // 8 directional edge features

        // Calculate gradients in different directions
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int idx = y * width + x;

                // Horizontal gradient
                int gx = grayPixels[idx + 1] - grayPixels[idx - 1];
                // Vertical gradient
                int gy = grayPixels[idx + width] - grayPixels[idx - width];

                // Calculate gradient magnitude and direction
                double magnitude = Math.sqrt(gx * gx + gy * gy);
                double angle = Math.atan2(gy, gx);

                // Quantize angle into 8 bins
                int bin = (int) ((angle + Math.PI) / (2 * Math.PI / 8)) % 8;
                edgeFeatures[bin] += magnitude;
            }
        }

        // Normalize features
        double sum = 0;
        for (double feature : edgeFeatures) {
            sum += feature;
        }
        if (sum > 0) {
            for (int i = 0; i < edgeFeatures.length; i++) {
                edgeFeatures[i] /= sum;
            }
        }

        return edgeFeatures;
    }

    /**
     * Extract texture features using variance in local regions
     */
    private double[] extractTextureFeatures(int[] grayPixels, int width, int height) {
        double[] textureFeatures = new double[16]; // 4x4 grid of texture measures

        int blockWidth = width / 4;
        int blockHeight = height / 4;

        for (int by = 0; by < 4; by++) {
            for (int bx = 0; bx < 4; bx++) {
                int startY = by * blockHeight;
                int startX = bx * blockWidth;
                int endY = Math.min(startY + blockHeight, height);
                int endX = Math.min(startX + blockWidth, width);

                // Calculate variance in this block
                double sum = 0;
                double sumSquares = 0;
                int count = 0;

                for (int y = startY; y < endY; y++) {
                    for (int x = startX; x < endX; x++) {
                        int pixel = grayPixels[y * width + x];
                        sum += pixel;
                        sumSquares += pixel * pixel;
                        count++;
                    }
                }

                if (count > 0) {
                    double mean = sum / count;
                    double variance = (sumSquares / count) - (mean * mean);
                    textureFeatures[by * 4 + bx] = Math.sqrt(variance);
                }
            }
        }

        // Normalize features
        double maxVariance = 0;
        for (double feature : textureFeatures) {
            maxVariance = Math.max(maxVariance, feature);
        }
        if (maxVariance > 0) {
            for (int i = 0; i < textureFeatures.length; i++) {
                textureFeatures[i] /= maxVariance;
            }
        }

        return textureFeatures;
    }

    /**
     * Compare two face encodings using multiple feature types with strict matching
     */
    public double compareFaces(String encoding1, String encoding2) {
        try {
            System.out.println("DEBUG: Encoding1 length: " + encoding1.length() + ", starts with: " + encoding1.substring(0, Math.min(50, encoding1.length())));
            System.out.println("DEBUG: Encoding2 length: " + encoding2.length() + ", starts with: " + encoding2.substring(0, Math.min(50, encoding2.length())));

            // Parse encodings - split only on first colon to separate hash from features
            int firstColon1 = encoding1.indexOf(":");
            int firstColon2 = encoding2.indexOf(":");

            if (firstColon1 == -1 || firstColon2 == -1) {
                System.out.println("Invalid encoding format - no colon found");
                return 0.0;
            }

            String hash1 = encoding1.substring(0, firstColon1);
            String features1 = encoding1.substring(firstColon1 + 1);
            String hash2 = encoding2.substring(0, firstColon2);
            String features2 = encoding2.substring(firstColon2 + 1);

            System.out.println("DEBUG: Hash1: " + hash1 + ", Hash2: " + hash2);

            // Compare hash parts first (quick check for identical images)
            if (hash1.equals(hash2)) {
                System.out.println("Identical hash found - 100% match");
                return 100.0; // Identical images
            }

            // Extract different feature types
            double histSimilarity = compareFeatureSection(features1, features2, "HIST:");
            double lbpSimilarity = compareFeatureSection(features1, features2, "LBP:");
            double edgeSimilarity = compareFeatureSection(features1, features2, "EDGE:");
            double textureSimilarity = compareFeatureSection(features1, features2, "TEXT:");

            // Weighted combination of all features (more conservative approach)
            double finalSimilarity = (histSimilarity * 0.3) +
                                   (lbpSimilarity * 0.4) +
                                   (edgeSimilarity * 0.2) +
                                   (textureSimilarity * 0.1);

            System.out.println("Feature similarities - Histogram: " + String.format("%.2f", histSimilarity) +
                             "%, LBP: " + String.format("%.2f", lbpSimilarity) +
                             "%, Edge: " + String.format("%.2f", edgeSimilarity) +
                             "%, Texture: " + String.format("%.2f", textureSimilarity) +
                             "%, Final: " + String.format("%.2f", finalSimilarity) + "%");

            return finalSimilarity;

        } catch (Exception e) {
            System.out.println("Face comparison error: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }

    /**
     * Compare a specific feature section between two encodings
     */
    private double compareFeatureSection(String encoding1, String encoding2, String featureType) {
        try {
            // Extract feature section from both encodings
            String[] features1 = extractFeatureSection(encoding1, featureType);
            String[] features2 = extractFeatureSection(encoding2, featureType);

            if (features1 == null || features2 == null || features1.length != features2.length) {
                return 0.0;
            }

            // Calculate cosine similarity for this feature type
            return calculateCosineSimilarity(features1, features2);

        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Extract a specific feature section from the encoding string
     */
    private String[] extractFeatureSection(String encoding, String featureType) {
        try {
            int startIdx = encoding.indexOf(featureType);
            if (startIdx == -1) return null;

            startIdx += featureType.length();
            int endIdx = encoding.indexOf(":", startIdx);
            if (endIdx == -1) endIdx = encoding.length();

            String section = encoding.substring(startIdx, endIdx);
            return section.split(",");

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Calculate similarity between two histograms
     */
    private double calculateHistogramSimilarity(String[] hist1, String[] hist2) {
        try {
            double correlation = 0.0;
            double sum1 = 0.0, sum2 = 0.0;
            double sumSq1 = 0.0, sumSq2 = 0.0;
            double sumProduct = 0.0;
            int n = Math.min(hist1.length - 1, hist2.length - 1); // -1 because last element is empty

            // Calculate means
            for (int i = 0; i < n; i++) {
                try {
                    double val1 = Double.parseDouble(hist1[i]);
                    double val2 = Double.parseDouble(hist2[i]);
                    sum1 += val1;
                    sum2 += val2;
                } catch (NumberFormatException e) {
                    // Skip invalid values
                }
            }

            double mean1 = sum1 / n;
            double mean2 = sum2 / n;

            // Calculate correlation coefficient
            for (int i = 0; i < n; i++) {
                try {
                    double val1 = Double.parseDouble(hist1[i]);
                    double val2 = Double.parseDouble(hist2[i]);

                    double diff1 = val1 - mean1;
                    double diff2 = val2 - mean2;

                    sumProduct += diff1 * diff2;
                    sumSq1 += diff1 * diff1;
                    sumSq2 += diff2 * diff2;
                } catch (NumberFormatException e) {
                    // Skip invalid values
                }
            }

            double denominator = Math.sqrt(sumSq1 * sumSq2);
            if (denominator > 0) {
                correlation = sumProduct / denominator;
            }

            // Convert correlation to similarity percentage (0-100)
            // Correlation ranges from -1 to 1, we want 0 to 100
            double similarity = ((correlation + 1) / 2) * 100;

            return Math.max(0, Math.min(100, similarity));

        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Calculate simple similarity between two histograms using intersection
     */
    private double calculateSimpleHistogramSimilarity(String[] hist1, String[] hist2) {
        try {
            double intersection = 0.0;
            double union = 0.0;
            int n = Math.min(hist1.length - 1, hist2.length - 1); // -1 because last element is empty

            for (int i = 0; i < n; i++) {
                try {
                    double val1 = Double.parseDouble(hist1[i]);
                    double val2 = Double.parseDouble(hist2[i]);

                    intersection += Math.min(val1, val2);
                    union += Math.max(val1, val2);
                } catch (NumberFormatException e) {
                    // Skip invalid values
                }
            }

            // Calculate Jaccard similarity (intersection over union)
            double similarity = union > 0 ? (intersection / union) * 100 : 0.0;

            return Math.max(0, Math.min(100, similarity));

        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Calculate cosine similarity between two histograms
     */
    private double calculateCosineSimilarity(String[] hist1, String[] hist2) {
        try {
            double dotProduct = 0.0;
            double norm1 = 0.0;
            double norm2 = 0.0;
            int n = Math.min(hist1.length - 1, hist2.length - 1);

            for (int i = 0; i < n; i++) {
                try {
                    double val1 = Double.parseDouble(hist1[i]);
                    double val2 = Double.parseDouble(hist2[i]);

                    dotProduct += val1 * val2;
                    norm1 += val1 * val1;
                    norm2 += val2 * val2;
                } catch (NumberFormatException e) {
                    // Skip invalid values
                }
            }

            double denominator = Math.sqrt(norm1 * norm2);
            if (denominator > 0) {
                double similarity = (dotProduct / denominator) * 100;
                return Math.max(0, Math.min(100, similarity));
            }
            return 0.0;

        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Calculate Euclidean similarity between two histograms
     */
    private double calculateEuclideanSimilarity(String[] hist1, String[] hist2) {
        try {
            double sumSquaredDiff = 0.0;
            double maxPossibleDistance = 0.0;
            int n = Math.min(hist1.length - 1, hist2.length - 1);

            for (int i = 0; i < n; i++) {
                try {
                    double val1 = Double.parseDouble(hist1[i]);
                    double val2 = Double.parseDouble(hist2[i]);

                    double diff = val1 - val2;
                    sumSquaredDiff += diff * diff;

                    // Calculate max possible distance for normalization
                    double maxVal = Math.max(val1, val2);
                    maxPossibleDistance += maxVal * maxVal;
                } catch (NumberFormatException e) {
                    // Skip invalid values
                }
            }

            if (maxPossibleDistance > 0) {
                double distance = Math.sqrt(sumSquaredDiff);
                double maxDistance = Math.sqrt(maxPossibleDistance);
                double similarity = (1.0 - (distance / maxDistance)) * 100;
                return Math.max(0, Math.min(100, similarity));
            }
            return 0.0;

        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Verify if a face matches with confidence above threshold
     */
    public boolean verifyFace(String knownEncoding, String testEncoding) {
        double similarity = compareFaces(knownEncoding, testEncoding);
        return similarity >= confidenceThreshold;
    }

    /**
     * Find best matching face from a list of known encodings with strict validation
     */
    public FaceMatchResult findBestMatch(String testEncoding, List<String> knownEncodings, List<String> employeeIds) {
        double bestSimilarity = 0.0;
        String bestMatchId = null;
        double secondBestSimilarity = 0.0;

        System.out.println("=== ENHANCED FACE MATCHING DEBUG ===");
        System.out.println("Number of registered employees: " + knownEncodings.size());
        System.out.println("Confidence threshold: " + confidenceThreshold + "% (SAME AS FRONTEND)");

        for (int i = 0; i < knownEncodings.size(); i++) {
            double similarity = compareFaces(knownEncodings.get(i), testEncoding);
            System.out.println("Employee ID " + employeeIds.get(i) + " similarity: " + String.format("%.2f", similarity) + "%");

            if (similarity > bestSimilarity) {
                secondBestSimilarity = bestSimilarity;
                bestSimilarity = similarity;
                bestMatchId = employeeIds.get(i);
                System.out.println("New best match: Employee ID " + bestMatchId + " with " + String.format("%.2f", bestSimilarity) + "%");
            } else if (similarity > secondBestSimilarity) {
                secondBestSimilarity = similarity;
            }
        }

        // Enhanced validation: Check if the best match is significantly better than the second best
        double confidenceGap = bestSimilarity - secondBestSimilarity;
        boolean isMatch = bestSimilarity >= confidenceThreshold;

        // Additional security: Require a minimum gap between best and second-best matches
        // This prevents false positives when multiple faces have similar low scores
        double minimumGap = 1.0; // Require at least 1% difference (reduced for better matching)
        if (isMatch && knownEncodings.size() > 1 && confidenceGap < minimumGap) {
            System.out.println("SECURITY WARNING: Best match confidence gap too small (" +
                             String.format("%.2f", confidenceGap) + "% < " + minimumGap + "%). Rejecting match.");
            isMatch = false;
        }

        if (!isMatch) {
            System.out.println("No reliable face match found. Best: " + String.format("%.2f", bestSimilarity) +
                             "%, Second: " + String.format("%.2f", secondBestSimilarity) +
                             "%, Gap: " + String.format("%.2f", confidenceGap) + "%");
            bestMatchId = null; // Clear the match ID if not reliable
        }

        System.out.println("=== FINAL RESULT ===");
        System.out.println("Best similarity: " + String.format("%.2f", bestSimilarity) + "%");
        System.out.println("Second best: " + String.format("%.2f", secondBestSimilarity) + "%");
        System.out.println("Confidence gap: " + String.format("%.2f", confidenceGap) + "%");
        System.out.println("Threshold: " + confidenceThreshold + "% (CONSISTENT WITH FRONTEND)");
        System.out.println("Match accepted: " + isMatch);

        if (isMatch && bestMatchId != null) {
            System.out.println("Matched Employee ID: " + bestMatchId);
        } else {
            System.out.println("No match - face not recognized");
        }

        return new FaceMatchResult(bestMatchId, bestSimilarity, isMatch);
    }

    /**
     * Result class for face matching
     */
    public static class FaceMatchResult {
        private final String employeeId;
        private final double confidence;
        private final boolean isMatch;

        public FaceMatchResult(String employeeId, double confidence, boolean isMatch) {
            this.employeeId = employeeId;
            this.confidence = confidence;
            this.isMatch = isMatch;
        }

        public String getEmployeeId() {
            return employeeId;
        }

        public double getConfidence() {
            return confidence;
        }

        public boolean isMatch() {
            return isMatch;
        }
    }
}
