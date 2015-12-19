package praxisprojekt.dboo.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PostgresHandler {

    public synchronized List<Contact> findAll(String stringFilter) {
        ArrayList arrayList = new ArrayList();

        try {
            //TODO get info from DB
        } catch (Exception ex) {
            Logger.getLogger(ContactService.class.getName()).log(
                    Level.SEVERE, null, ex);
        }

        // TODO change sort
        Collections.sort(arrayList, new Comparator<Contact>() {

            @Override
            public int compare(Contact o1, Contact o2) {
                return (int) (o2.getId() - o1.getId());
            }
        });
        return arrayList;
    }

    public void insert(String table, String values) {
        // TODO
    }

    public void update(String table, String values) {
        // TODO
    }

    public void delete(String table, String values) {
        // TODO
    }
}
