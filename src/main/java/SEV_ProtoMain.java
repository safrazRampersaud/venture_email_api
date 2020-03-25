import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class SEV_ProtoMain {
    private static final String APPLICATION_NAME = "SEV_Prototype";
    private static final String SUBJECT_TOKEN = "<>";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens / folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.MAIL_GOOGLE_COM);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.

        // Here is where access to a DB should work



        InputStream in = SEV_ProtoMain.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static String gmailFormat(Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        return sdf.format(d);
    }

    /**
     * Get a Message and use it to create a MimeMessage.
     *
     * @param service Authorized Gmail API instance.
     * @param userId User's email address. The special value "me"
     * can be used to indicate the authenticated user.
     * @param messageId ID of Message to retrieve.
     * @return MimeMessage MimeMessage populated from retrieved Message.
     * @throws IOException
     * @throws MessagingException
     */
    public static MimeMessage getMimeMessage(Gmail service, String userId, String messageId)
            throws IOException, MessagingException {
        Message message = service.users().messages().get(userId, messageId).setFormat("raw").execute();

        Base64 base64Url = new Base64(true);
        byte[] emailBytes = base64Url.decodeBase64(message.getRaw());

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session, new ByteArrayInputStream(emailBytes));

        return email;
    }

    public static void main(String... args) throws IOException, GeneralSecurityException, MessagingException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Print the labels in the user's account.
        String user = "safraz.rampersaud@gmail.com";
        ListMessagesResponse messagesResponse = service.users().messages().list(user).execute();

        Gmail.Users.Messages.List request = service.users().messages().list(user)
                // or setQ("is:sent after:yyyy/MM/dd before:yyyy/MM/dd")
                .setLabelIds(Arrays.asList("INBOX"))
//                .setQ("after:" + gmailFormat(new Date("2020/02/27")) + " "+
//                        "before:"+ gmailFormat(new Date("2020/02/29")))
                .setQ("after:" + gmailFormat(new Date("2020/03/25")) + " "+
                        "before:"+ gmailFormat(new Date("2020/03/26")))

                .setMaxResults(1000L);
        List<Message> list = new LinkedList<>();
        ListMessagesResponse response = null;

        do {
            response = request.execute();
            list.addAll(response.getMessages());
            request.setPageToken(response.getNextPageToken());
        } while (request.getPageToken() != null && request.getPageToken().length() > 0);

        Map<String, Integer> clientFrequencyMap = new HashMap<>();

        for(Message item : list){
            Message executeResponse = service.users().messages().get(user, item.getId())
                    .setFormat("full")
                    .execute();

                String accessFrom = getMimeMessage(service, user, executeResponse.getId()).getHeader("From")[0];
                String accessTo = getMimeMessage(service, user, executeResponse.getId()).getHeader("To")[0];
                String accessSubject = getMimeMessage(service, user, executeResponse.getId()).getHeader("Subject")[0];
                Long accessSendTime = executeResponse.getInternalDate();

            DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss Z");
            formatter.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

            formatter.format(accessSendTime);

            System.out.println(formatter.format(accessSendTime));

            formatter.setTimeZone(TimeZone.getTimeZone("America/New_York"));
            System.out.println(formatter.format(accessSendTime));

                    System.out.println("From:\t" + accessFrom);
                    System.out.println("To:\t" + accessTo);
                    System.out.println("Subject:\t" + accessSubject);
                    System.out.println("Internal Date:\t" + accessSendTime);
                    System.out.println();

                    if(accessSubject.split(SUBJECT_TOKEN).length > 1){
                        System.out.println("FOUND CLIENT\n");
                        String[] splitSubject = accessSubject.split(SUBJECT_TOKEN);
                        String keyBase = splitSubject[0].trim();
                        String entryBase = splitSubject[1].trim();
                        if(clientFrequencyMap.containsKey(entryBase)){
                            clientFrequencyMap.put(entryBase, clientFrequencyMap.get(entryBase) + 1);
                        } else {
                            clientFrequencyMap.put(entryBase, 1);
                        }
                        clientFrequencyMap.forEach((key, value) -> System.out.println("Client: " + key + ", Frequency: " + value + "\n"));

                        System.out.println("CLIENT PROFILE\n");

                        // Get this.name on ProfileStructure
                        String[] nameBuilder = accessTo.split(",");
                        String[] nameBuilderIsolate = nameBuilder[1].split("<");
                        String nameBuilderAccess = nameBuilderIsolate[0].trim();

                        // Get this.email on ProfileStructure
                        String[] emailBuilder = nameBuilderIsolate[1].split(">");
                        String emailBuilderAccess = emailBuilder[0].trim();

                        // Get this.company on ProfileStructure
                        String[] companyBuilder = emailBuilderAccess.split("@");
                        String[] companyBuilderIsolate = companyBuilder[1].split("\\.");
                        String companyBuilderAccess = companyBuilderIsolate[0].trim();

                        // Get this.intro on ProfileStructure
                        String introAccess = accessFrom.split("<")[0].trim();

                        // Get this.secondary on ProfileStructure
                        String secondaryAccess = nameBuilder[0].split("<")[0].trim();

                        // Get this.fromEmail on ProfileStructure
                        String fromEmailAccess = accessFrom.split("<")[1].split(">")[0].trim();

                        // Get this.fromCompany on ProfileStructure
                        String fromCompanyAccess = accessFrom.split("<")[1].split(">")[0].split("@")[1].split("\\.")[0].trim();

                        ProfileStructure profileInstance = new ProfileStructure(
                            nameBuilderAccess,
                            "Data Scientist", //Access from external source
                            emailBuilderAccess,
                            companyBuilderAccess,
                            introAccess,
                            "CEO", //Access from external source
                            secondaryAccess,
                            fromEmailAccess,
                            fromCompanyAccess
                        );
                        System.out.println(profileInstance.toString());
                    }
        }
    }
}