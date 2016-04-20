package bot;

import net.coobird.thumbnailator.Thumbnails;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;

public class Utils {

    private static int idx = 0;

    private final static String[] KEYS = {
            "AIzaSyBwHV-De4PF9HgaVNeAvXqtSeLj2Nyg0fk",
            "AIzaSyDDbWrVetwzu0_dI1VTwGooa9Gy3ISwR4o",
            "AIzaSyDY18lHtBRspOGnZBEYHN_QzzEcWfD5pK4",
            "AIzaSyBan1-iph_HqwqIglN1lRiloDX9-qVNPLU",
            "AIzaSyAbpvmJxFlaVnlgfd5Qhc-L4JwWp9UJsw0",
            "AIzaSyCpYumpxDKjErEkjlHOxh6OpSduPCM8JV4",
            "AIzaSyCq2N3-Y7mC4VuqUhrx8OX60unB14InE-g",
            "AIzaSyA3RX2RWCpjbgyjEXaiJ47NK8SI-VcQ-dE"
    };

    private final static String BASE_URL_1 = "https://www.googleapis.com/customsearch/v1?q=",
            BASE_URL_2 = "&cx=005581394676374455442%3Afihmnxuedsw&hl=fr&num=1&rights=cc_attribute&searchType=image&key=";

    private static String getContent(String link) {
        try {
            URL url = new URL(BASE_URL_1 + link.replaceAll(" ", "+") + BASE_URL_2 + KEYS[idx]);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(url.openStream()));
            StringBuilder sb = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine).append("\n");
            }
            in.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static Image_java parseRequest(String page) {
        try {
            String content = getContent(page);
            if (content.isEmpty()) {
                idx++;
                System.out.println("Change code");
                content = getContent(page);
            }
            JSONObject obj = new JSONObject(content);
            try {
                if (obj.getJSONObject("error").getString("code").equals("403")) {
                    idx++;
                    System.out.println("Change code");
                    obj = new JSONObject(getContent(page));
                }
            } catch (Exception e) {

            }
            JSONObject results = obj.getJSONArray("items").getJSONObject(0);
            return new Image_java(page, results.getString("link"), results.getJSONObject("image").getString("thumbnailLink"), results.getString("snippet"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean downloadImageFromURL(String link, String folder, String name) {
        try {
            URL url = new URL(link);
            InputStream in = new BufferedInputStream(url.openStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n = 0;
            while (-1 != (n = in.read(buf))) {
                out.write(buf, 0, n);
            }
            out.close();
            in.close();
            byte[] response = out.toByteArray();
            File f = new File(folder);
            f.mkdirs();
            FileOutputStream fos = new FileOutputStream(name);
            fos.write(response);
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void resize(String path) {
        try {
            File f = new File(path);
            Thumbnails.of(f)
                    .size(640, 640)
                    .outputFormat("jpg")
                    .toFile(f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
