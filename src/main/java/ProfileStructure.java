
public class ProfileStructure {

    String name;
    String title;
    String email;
    String company;
    String intro;
    String expertise;
    String introEmail;
    String introCompany;
    String secondary;

    public ProfileStructure(String _name, String _title, String _email, String _company, String _intro, String _expertise, String _secondary, String _introEmail, String _introCompany){
        this.name = _name;
        this.title = _title;
        this.email = _email;
        this.company = _company;
        this.intro = _intro;
        this.expertise = _expertise;
        this.secondary = _secondary;
        this.introEmail = _introEmail;
        this.introCompany = _introCompany;
    }

    @Override
    public String toString() {
        return "ProfileStructure{" +
                "name='" + name + '\'' +
                ", expertise='" + title + '\'' +
                ", email='" + email + '\'' +
                ", company='" + company + '\'' +
                ", intro='" + intro + '\'' +
                ", introTitle='" + expertise + '\'' +
                ", introEmail='" + introEmail + '\'' +
                ", introCompany='" + introCompany + '\'' +
                ", secondary='" + secondary + '\'' +
                '}';
    }
}
