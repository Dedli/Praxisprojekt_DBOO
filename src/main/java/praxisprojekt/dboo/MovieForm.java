package praxisprojekt.dboo;

import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.event.FieldEvents;
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
    TextField filmname = new TextField("Filmname");
    TextField jahr = new TextField("Erscheinungsjahr");
    TextField regisseur = new TextField("Regisseur");
    TextField schauspieler = new TextField("Schauspieler");

    Button difference = new Button("Difference", this::nf2_difference);
    Button intersection = new Button("Intersection", this::nf2_intersection);
    Button union = new Button("Union", this::nf2_union);
    Button subset = new Button("Subset", this::nf2_subset);
    Button properSubset = new Button("Proper Subset", this::nf2_properSubset);
    Button equal = new Button("Equal", this::nf2_equal);
    Button notEqual = new Button("Not Equal", this::nf2_notEqual);
    Label result = new Label("Ergebnis: ");

    ComboBox moviePicker = new ComboBox("Wähle einen Film:");

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
        cancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);
        setVisible(false);
    }

    private void buildLayout() {
        setSizeUndefined();
        setMargin(true);

        HorizontalLayout actions = new HorizontalLayout(save, cancel);
        actions.setSpacing(true);

        HorizontalLayout firstRow = new HorizontalLayout(union, intersection, difference);
        firstRow.setSpacing(true);
        HorizontalLayout secondRow = new HorizontalLayout(subset, properSubset);
        secondRow.setSpacing(true);
        HorizontalLayout thirdRow = new HorizontalLayout(equal, notEqual);
        thirdRow.setSpacing(true);
        VerticalLayout lowerButtons = new VerticalLayout(firstRow, secondRow, thirdRow);
        lowerButtons.setSpacing(true);

        difference.setDescription("zeigt die Differenz zwischen Schauspielern und Regisseur");
        intersection.setDescription("zeigt die gemeinsamen Personen in Schauspieler und Regisseur");
        union.setDescription("zeigt alle Personen aus den Schauspielern und Regisseuren");

		addComponents(actions, filmname, regisseur, jahr, schauspieler, lowerButtons, moviePicker, result);
        result.setVisible(false);
        moviePicker.addItem("test");
        moviePicker.addItem("anderer");
        moviePicker.setVisible(false);
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

            String msg = String.format("'%s' gespeichert.",
                    movie.getFilmname());
            Notification.show(msg,Type.TRAY_NOTIFICATION);
            getUI().refreshMovies();
        } catch (FieldGroup.CommitException e) {
            // Validation exceptions could be shown here
        }
    }

    public void cancel(Button.ClickEvent event) {
        // Place to call business logic.
        Notification.show("Abgebrochen", Type.TRAY_NOTIFICATION);
        getUI().refreshMovies();
        getUI().movieList.select(null);
    }

    void edit(Movie movie) {
        this.movie = movie;
        if(movie != null) {
            // Bind the properties of the contact POJO to fiels in this form
            formFieldBindings = BeanFieldGroup.bindFieldsBuffered(movie, this);
            filmname.focus();
        }
        setVisible(movie != null);
    }

    @Override
    public MovieUI getUI() {
        return (MovieUI) super.getUI();
    }

    // nf2-buttons
    public void nf2_difference(Button.ClickEvent event) {
        resetButtons();
        difference.setEnabled(false);
        result.setVisible(false);
        moviePicker.setVisible(true);
        moviePicker.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                result.setVisible(true);
                result.setCaption("Difference");
                result.setValue("[..., ..., ...]");
            }
        });
    }
    public void nf2_intersection(Button.ClickEvent event) {
        resetButtons();
        intersection.setEnabled(false);
        result.setVisible(false);
        moviePicker.setVisible(true);
        moviePicker.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                result.setVisible(true);
                result.setCaption("Intersection");
                result.setValue("[..., ..., ...]");
            }
        });
    }
    public void nf2_union(Button.ClickEvent event) {
        resetButtons();
        union.setEnabled(false);
        result.setVisible(false);
        moviePicker.setVisible(true);
        moviePicker.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                result.setVisible(true);
                result.setCaption("Union");
                result.setValue("[..., ..., ...]");
            }
        });
    }
    public void nf2_subset(Button.ClickEvent event) {
        resetButtons();
        subset.setEnabled(false);
        result.setVisible(false);
        moviePicker.setVisible(true);
        moviePicker.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                result.setVisible(true);
                result.setCaption("Subset");
                result.setValue("true or false");
            }
        });
    }
    public void nf2_properSubset(Button.ClickEvent event) {
        resetButtons();
        properSubset.setEnabled(false);
        result.setVisible(false);
        moviePicker.setVisible(true);
        moviePicker.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                result.setVisible(true);
                result.setCaption("proper Subset");
                result.setValue("true or false");
            }
        });
    }
    public void nf2_equal(Button.ClickEvent event) {
        resetButtons();
        equal.setEnabled(false);
        result.setVisible(false);
        moviePicker.setVisible(true);
        moviePicker.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                result.setVisible(true);
                result.setCaption("Equal");
                result.setValue("true or false");
            }
        });
    }
    public void nf2_notEqual(Button.ClickEvent event) {
        resetButtons();
        notEqual.setEnabled(false);
        result.setVisible(false);
        moviePicker.setVisible(true);
        moviePicker.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                result.setVisible(true);
                result.setCaption("not Equal");
                result.setValue("true or false");
            }
        });
    }
    public void resetButtons(){
        moviePicker.select(moviePicker.getNullSelectionItemId());
        intersection.setEnabled(true);
        difference.setEnabled(true);
        union.setEnabled(true);
        subset.setEnabled(true);
        properSubset.setEnabled(true);
        equal.setEnabled(true);
        notEqual.setEnabled(true);
    }
}
