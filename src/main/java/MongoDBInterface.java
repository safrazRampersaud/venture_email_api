import com.mongodb.MongoClient;

public class MongoDBInterface {
    MongoClient mongoClient = new MongoClient("localhost", 27017);

   // mongoClient.getDatabaseNames().forEach(System.out::println);


}
