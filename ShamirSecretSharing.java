import org.json.JSONObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.math.BigInteger;

public class ShamirSecretSharing {

    public static BigInteger decodeValue(int base, String value) {
        return new BigInteger(value, base);
    }

    public static JSONObject readInput(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        return new JSONObject(content);
    }

    public static List<BigInteger[]> extractPoints(JSONObject data) {
        int k = data.getJSONObject("keys").getInt("k");
        List<BigInteger[]> points = new ArrayList<>();
        
        for (String key : data.keySet()) {
            if (key.equals("keys")) continue;
            int x = Integer.parseInt(key);
            JSONObject val = data.getJSONObject(key);
            BigInteger y = decodeValue(val.getInt("base"), val.getString("value"));
            points.add(new BigInteger[]{BigInteger.valueOf(x), y});
        }
        
        points.sort((a, b) -> a[0].compareTo(b[0]));
        return points.subList(0, k);
    }

    public static BigInteger solvePolynomial(List<BigInteger[]> points) {
        int k = points.size();
        BigInteger[][] X = new BigInteger[k][k];
        BigInteger[] Y = new BigInteger[k];

        for (int i = 0; i < k; i++) {
            BigInteger x = points.get(i)[0];
            Y[i] = points.get(i)[1];
            for (int j = 0; j < k; j++) {
                X[i][j] = x.pow(k - j - 1);
            }
        }
        
        BigInteger[] coefficients = solveLinearSystem(X, Y);
        return coefficients[k - 1]; // Constant term
    }

    public static BigInteger[] solveLinearSystem(BigInteger[][] X, BigInteger[] Y) {
        int n = Y.length;
        BigInteger[] coefficients = new BigInteger[n];

        for (int i = 0; i < n; i++) {
            int maxRow = i;
            for (int j = i + 1; j < n; j++) {
                if (X[j][i].abs().compareTo(X[maxRow][i].abs()) > 0) {
                    maxRow = j;
                }
            }

            BigInteger[] temp = X[i];
            X[i] = X[maxRow];
            X[maxRow] = temp;

            BigInteger t = Y[i];
            Y[i] = Y[maxRow];
            Y[maxRow] = t;

            for (int j = i + 1; j < n; j++) {
                BigInteger factor = X[j][i].divide(X[i][i]);
                Y[j] = Y[j].subtract(factor.multiply(Y[i]));
                for (int k = i; k < n; k++) {
                    X[j][k] = X[j][k].subtract(factor.multiply(X[i][k]));
                }
            }
        }

        for (int i = n - 1; i >= 0; i--) {
            BigInteger sum = BigInteger.ZERO;
            for (int j = i + 1; j < n; j++) {
                sum = sum.add(X[i][j].multiply(coefficients[j]));
            }
            coefficients[i] = Y[i].subtract(sum).divide(X[i][i]);
        }

        return coefficients;
    }

    public static void main(String[] args) throws IOException {
        String[] filePaths = {"test_case1.json", "test_case2.json"}; // Update with actual file paths
        for (String filePath : filePaths) {
            JSONObject data = readInput(filePath);
            List<BigInteger[]> points = extractPoints(data);
            BigInteger secret = solvePolynomial(points);
            System.out.println("Secret (c) for " + filePath + ": " + secret);
        }
    }
}
