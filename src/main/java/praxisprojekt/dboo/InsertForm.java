package praxisprojekt.dboo;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.themes.ValoTheme;
import praxisprojekt.dboo.backend.Movie;

public class InsertForm extends FormLayout {

    // all components for the insertform are defined here
    Button save = new Button("Erstellen", this::save);
    Button cancel = new Button("Abbruch", this::cancel);
    TextField filmname = new TextField("Filmname");
    TextField jahr = new TextField("Erscheinungsjahr");
    TextField genre = new TextField("Genre");
    TextField regisseur = new TextField("Regisseur");
    // TextField schauspieler = new TextField("Schauspieler");
    TextArea schauspieler = new TextArea("Schauspieler");

    Movie movie;

    // Easily bind forms to beans and manage validation and buffering
    BeanFieldGroup<Movie> formFieldBindings;

    public InsertForm() {
        configureComponents();
        buildLayout();
    }

    private void configureComponents() {
        // configure button shortcuts and style theme
        save.setStyleName(ValoTheme.BUTTON_PRIMARY);
        save.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        cancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
        setVisible(false);
    }

    private void buildLayout() {
        setSizeUndefined();
        setMargin(true);

        HorizontalLayout actions = new HorizontalLayout(save, cancel);
        actions.setSpacing(true);
        jahr.setWidth("65px");
        schauspieler.setRows(3);
        addComponents(actions, filmname, regisseur, jahr, genre, schauspieler);
    }

    public void save(Button.ClickEvent event) {
        try {
            // Commit the fields from UI to DAO
            formFieldBindings.commit();

            // Save DAO to backend with direct synchronous service API
            getUI().service.save(movie);
            // run method for database push
            getUI().service.saveToDb(movie);

            String msg = String.format("'%s' gespeichert.",
                    movie.getFilmname());
            Notification.show(msg,Type.TRAY_NOTIFICATION);
            getUI().refreshMovies();
        } catch (FieldGroup.CommitException e) {
            // Validation exceptions could be shown here
        }
    }

    public void cancel(Button.ClickEvent event) {
        // hides the sidebar and restores full-size grid
        Notification.show("Abgebrochen", Type.TRAY_NOTIFICATION);
        getUI().refreshMovies();
        getUI().movieList.select(null);
    }

    void edit(Movie movie) {
        this.movie = movie;
        if(movie != null) {
            // Bind the properties of the contact POJO to fields in this form
            formFieldBindings = BeanFieldGroup.bindFieldsBuffered(movie, this);
            filmname.focus();
        }
        setVisible(movie != null);
    }

    @Override
    public MovieUI getUI() {
        return (MovieUI) super.getUI();
    }
}
