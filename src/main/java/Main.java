public class Main {
    public static final int PORT = 8989;
    private static final String PATH_PDF_FILE = "pdfs";
    public static String STOP_WORDS_FILE_PATH = "stop-ru.txt";

    public static void main(String[] args) throws Exception {

        SearchServer server = new SearchServer(PORT, PATH_PDF_FILE);
        server.start();
    }
}
