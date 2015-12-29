package praxisprojekt.dboo.backend;

import org.apache.commons.beanutils.BeanUtils;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Separate Java service class.
 * Backend implementation for the address book application, with "detached entities"
 * simulating real world DAO. Typically these something that the Java EE
 * or Spring backend services provide.
 */
// Backend service class. This is just a typical Java backend implementation
// class and nothing Vaadin specific.
public class MovieService {

    // Create dummy data by randomly combining first and last names
    static String[] fnames = { "Peter", "Alice", "John", "Mike", "Olivia",
            "Nina", "Alex", "Rita", "Dan", "Umberto", "Henrik", "Rene", "Lisa",
            "Linda", "Timothy", "Daniel", "Brian", "George", "Scott",
            "Jennifer" };
    static String[] lnames = { "Smith", "Johnson", "Williams", "Jones",
            "Brown", "Davis", "Miller", "Wilson", "Moore", "Taylor",
            "Anderson", "Thomas", "Jackson", "White", "Harris", "Martin",
            "Thompson", "Young", "King", "Robinson" };

    private static MovieService instance;

    public static MovieService createDemoService() {
        if (instance == null) {

            final MovieService movieService = new MovieService();

            Random r = new Random(0);
            Calendar cal = Calendar.getInstance();
            for (int i = 0; i < 100; i++) {
                Movie contact = new Movie();
                contact.setFilmName(fnames[r.nextInt(fnames.length)]);
                contact.setLastName(lnames[r.nextInt(fnames.length)]);
                contact.setEmail(contact.getFilmName().toLowerCase() + "@"
                        + contact.getLastName().toLowerCase() + ".com");
                contact.setYear("19" + (r.nextInt(90)));
                cal.set(1930 + r.nextInt(70),
                        r.nextInt(11), r.nextInt(28));
                contact.setBirthDate(cal.getTime());
                movieService.save(contact);
            }
            instance = movieService;
        }

        return instance;
    }

    private HashMap<Long, Movie> movies = new HashMap<>();
    private long nextId = 0;

    public synchronized List<Movie> findAll(String stringFilter) {
        ArrayList arrayList = new ArrayList();
        for (Movie movie : movies.values()) {
            try {
                boolean passesFilter = (stringFilter == null || stringFilter.isEmpty())
                        || movie.toString().toLowerCase()
                                .contains(stringFilter.toLowerCase());
                if (passesFilter) {
                    arrayList.add(movie.clone());
                }
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(MovieService.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }
        Collections.sort(arrayList, new Comparator<Movie>() {

            @Override
            public int compare(Movie o1, Movie o2) {
                return (int) (o2.getId() - o1.getId());
            }
        });
        return arrayList;
    }

    public synchronized long count() {
        return movies.size();
    }

    public synchronized void delete(Movie value) {
        movies.remove(value.getId());
    }

    public synchronized void save(Movie entry) {
        if (entry.getId() == null) {
            entry.setId(nextId++);
        }
        try {
            entry = (Movie) BeanUtils.cloneBean(entry);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        movies.put(entry.getId(), entry);
    }

}
