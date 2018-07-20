package co.nos.noswallet.kyc.identity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

public class IdentityMapper {

    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());


    @Inject
    IdentityMapper() {

    }

    public String formatDate(Date date) {
        return dateFormat.format(date);
    }

    public boolean isBirthDateValid(String text) {
        try {
            Date date = dateFormat.parse(text);
            return date != null;
        } catch (ParseException x) {
            return false;
        }
    }

    public boolean areIdentityValid(String firstName, String lastName, String birthDate) {
        return firstName.length() > 0 && lastName.length() > 0 && isBirthDateValid(birthDate);
    }
}
