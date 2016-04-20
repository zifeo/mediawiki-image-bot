package bot;

public class Image_java {

    private final String page, orig, thumb, snippet;

    public Image_java(String page, String orig, String thumb, String snippet) {
        this.page = page;
        this.orig = orig;
        this.thumb = thumb;
        this.snippet = snippet;
        System.out.println(toString());
    }

    public Image_java(String page, String orig, String snippet) {
        this(page, orig, "", snippet);
    }

    public void saveToFile() {
        saveImageToFile(orig, true);
        saveImageToFile(thumb, false);
    }

    private void saveImageToFile(String s, boolean orig) {
        if (s != null) {
            String fileName = "download/" + page + "/" + snippet.toLowerCase().replaceAll(" ", "_") + "_" + (orig ? "original" : "thumbnail") + ".jpg";
            Utils.downloadImageFromURL(s, "download/" + page + "/", fileName);
            if (orig)
                Utils.resize(fileName);
        }
    }

    @Override
    public String toString() {
        return String.format("Snippet : %s\nLink : %s\nThumbnail : %s", snippet, orig, thumb);
    }
}
