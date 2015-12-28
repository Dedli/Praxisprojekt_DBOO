package praxisprojekt.dboo;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.themes.ValoTheme;
import praxisprojekt.dboo.backend.Movie;

/* Create custom UI Components.
 * Testing
 * Create your own Vaadin components by inheritance and composition.
 * This is a form component inherited from VerticalLayout. Use
 * Use BeanFieldGroup to bind data fields from DTO to UI fields.
 * Similarly named field by naming convention or customized
 * with @PropertyId annotation.
 */
public class MovieForm extends FormLayout {

    Button save = new Button("Speichern", this::save);
    Button cancel = new Button("Abbruch", this::cancel);
    TextField firstName = new TextField("Filmname");
    TextField lastName = new TextField("Erscheinungsjahr");
    TextField phone = new TextField("Phone");
    TextField email = new TextField("Email");
    DateField birthDate = new DateField("Birth date");

    Movie movie;

    // Easily bind forms to beans and manage validation and buffering
    BeanFieldGroup<Movie> formFieldBindings;

    public MovieForm() {
        configureComponents();
        buildLayout();
    }

    private void configureComponents() {
        /* Highlight primary actions.
         *
         * With Vaadin built-in styles you can highlight the primary save button
         * and give it a keyboard shortcut for a better UX.
         */
        save.setStyleName(ValoTheme.BUTTON_PRIMARY);
        save.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        setVisible(false);
    }

    private void buildLayout() {
        setSizeUndefined();
        setMargin(true);

        HorizontalLayout actions = new HorizontalLayout(save, cancel);
        actions.setSpacing(true);

		addComponents(actions, firstName, lastName, phone, email, birthDate);
    }

    /* Use any JVM language.
     *
     * Vaadin supports all languages supported by Java Virtual Machine 1.6+.
     * This allows you to program user interface in Java 8, Scala, Groovy or any other
     * language you choose.
     * The new languages give you very powerful tools for organizing your code
     * as you choose. For example, you can implement the listener methods in your
     * compositions or in separate controller classes and receive
     * to various Vaadin component events, like button clicks. Or keep it simple
     * and compact with Lambda expressions.
     */
    public void save(Button.ClickEvent event) {
        try {
            // Commit the fields from UI to DAO
            formFieldBindings.commit();

            // Save DAO to backend with direct synchronous service API
            getUI().service.save(movie);

            String msg = String.format("Saved '%s %s'.",
                    movie.getFirstName(),
                    movie.getLastName());
            Notification.show(msg,Type.TRAY_NOTIFICATION);
            getUI().refreshMovies();
        } catch (FieldGroup.CommitException e) {
            // Validation exceptions could be shown here
        }
    }

    public void cancel(Button.ClickEvent event) {
        // Place to call business logic.
        Notification.show("Cancelled", Type.TRAY_NOTIFICATION);
        getUI().movieList.select(null);
    }

    void edit(Movie movie) {
        this.movie = movie;
        if(movie != null) {
            // Bind the properties of the contact POJO to fiels in this form
            formFieldBindings = BeanFieldGroup.bindFieldsBuffered(movie, this);
            firstName.focus();
        }
        setVisible(movie != null);
    }

    @Override
    public AddressbookUI getUI() {
        return (AddressbookUI) super.getUI();
    }

}
