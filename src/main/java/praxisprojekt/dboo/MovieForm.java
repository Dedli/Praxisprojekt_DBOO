package praxisprojekt.dboo;

import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.themes.ValoTheme;
import praxisprojekt.dboo.backend.Movie;

import java.util.HashMap;

/* Create custom UI Components.
 * Testing
 * Create your own Vaadin components by inheritance and composition.
 * This is a form component inherited from VerticalLayout. Use
 * Use BeanFieldGroup to bind data fields from DTO to UI fields.
 * Similarly named field by naming convention or customized
 * with @PropertyId annotation.
 */
public class MovieForm extends FormLayout {

    // all components for the movieform are defined here
    Button save = new Button("Speichern", this::change);
    Button cancel = new Button("Abbruch", this::cancel);
    Button delete = new Button("Löschen", this::delete);
    TextField filmname = new TextField("Filmname");
    TextField jahr = new TextField("Erscheinungsjahr");
    TextField regisseur = new TextField("Regisseur");
    TextField genre = new TextField("Genre");
    TextArea schauspieler = new TextArea("Schauspieler");

    Button difference = new Button("\u2215", this::nf2_difference);
    Button intersection = new Button("\u2229", this::nf2_intersection);
    Button union = new Button("\u222A", this::nf2_union);
    Button subset = new Button("\u2286", this::nf2_subset);
    Button properSubset = new Button("\u2282", this::nf2_properSubset);
    Button equal = new Button("\u003D", this::nf2_equal);
    Button notEqual = new Button("\u2260", this::nf2_notEqual);

    Panel resultPanel = new Panel();
    Label result = new Label(" ");

    ComboBox columnPicker = new ComboBox("Spaltenauswahl:");
    ComboBox moviePicker = new ComboBox("Filmauswahl:");

    Movie movie;

    // Easily bind forms to beans and manage validation and buffering
    BeanFieldGroup<Movie> formFieldBindings;

    public MovieForm() {
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

        // construct top buttons
        // --commented out delete button
        HorizontalLayout actions = new HorizontalLayout(save, cancel, delete);
        // HorizontalLayout actions = new HorizontalLayout(save, cancel);
        actions.setSpacing(true);

        // construct operator button layout
        HorizontalLayout firstRow = new HorizontalLayout(union, intersection, difference);
        firstRow.setSpacing(true);
        HorizontalLayout secondRow = new HorizontalLayout(subset, properSubset);
        secondRow.setSpacing(true);
        HorizontalLayout thirdRow = new HorizontalLayout(equal, notEqual);
        thirdRow.setSpacing(true);
        VerticalLayout lowerButtons = new VerticalLayout(firstRow, secondRow, thirdRow);
        lowerButtons.setSpacing(true);

        difference.setDescription("Differenz / Difference");
        intersection.setDescription("Schnittmenge / Intersection");
        union.setDescription("Vereinigung / Union");
        subset.setDescription("Teilmenge / Subset");
        properSubset.setDescription("Echte Teilmenge / Proper Subset");
        equal.setDescription("Gleich / Equal");
        notEqual.setDescription("Ungleich / not Equal");
        schauspieler.setRows(2);

        result.setSizeUndefined();
        result.setWidth("220px");
        resultPanel.setWidth("225px");
        resultPanel.setHeight("100px");
        resultPanel.setContent(result);
        resultPanel.setVisible(false);
        // resultPanel.getContent().setSizeUndefined();
        filmname.setWidth("335px");
        regisseur.setWidth("335px");
        genre.setWidth("335px");
        schauspieler.setWidth("335px");
        jahr.setWidth("59px");
        addComponents(actions, filmname, regisseur, jahr, genre, schauspieler, lowerButtons, columnPicker,moviePicker, resultPanel);
        result.setVisible(false);
        equal.setWidth("50px");
        notEqual.setWidth("50px");
        union.setWidth("50px");
        subset.setWidth("50px");
        intersection.setWidth("50px");
        properSubset.setWidth("50px");
        difference.setWidth("50px");
        // add columns to picker
        columnPicker.setVisible(false);
        columnPicker.addItem("Genre");
        columnPicker.addItem("Schauspieler");
        columnPicker.addItem("Regisseur");
        moviePicker.setVisible(false);
    }

    public void change(Button.ClickEvent event) {
        try {
            // Commit the fields from UI to DAO
            formFieldBindings.commit();

            // Save DAO to backend with direct synchronous service API
            getUI().service.save(movie);
            getUI().service.updateToDb(movie);

            String msg = String.format("'%s' gespeichert.",
                    movie.getFilmname());
            Notification.show(msg,Type.TRAY_NOTIFICATION);
            getUI().refreshMovies();
        } catch (FieldGroup.CommitException e) {
            // Validation exceptions could be shown here
        }
    }

    public void delete(Button.ClickEvent event) {
        try {
            // Commit the fields from UI to DAO
            formFieldBindings.commit();

            // Save DAO to backend with direct synchronous service API
            getUI().service.delete(movie);
            getUI().service.deleteFromDb(movie);

            String msg = String.format("'%s' gelöscht.",
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
        resetButtons();
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

    // methods for nf2-buttons
    public void nf2_difference(Button.ClickEvent event) {
        resetButtons();
        difference.setEnabled(false);
        showPicker("Difference");
    }
    public void nf2_intersection(Button.ClickEvent event) {
        resetButtons();
        intersection.setEnabled(false);
        showPicker("Intersection");
    }
    public void nf2_union(Button.ClickEvent event) {
        resetButtons();
        union.setEnabled(false);
        showPicker("Union");
    }
    public void nf2_subset(Button.ClickEvent event) {
        resetButtons();
        subset.setEnabled(false);
        showPicker("Subset");
    }
    public void nf2_properSubset(Button.ClickEvent event) {
        resetButtons();
        properSubset.setEnabled(false);
        showPicker("Proper Subset");
    }
    public void nf2_equal(Button.ClickEvent event) {
        resetButtons();
        equal.setEnabled(false);
        showPicker("Equal");
    }
    public void nf2_notEqual(Button.ClickEvent event) {
        resetButtons();
        notEqual.setEnabled(false);
        showPicker("Not Equal");
    }

    // shows the two pickers for column and movie
    // runs nf2_query with specific functionality parameter
    public void showPicker(String functionality) {
        columnPicker.setVisible(true);
        columnPicker.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                System.out.println(columnPicker.getValue());
                if(moviePicker.getValue() != null){
                    System.out.println(moviePicker.getValue());
                    String resultString = getUI().service.nf2_query(functionality, (String)columnPicker.getValue(), movie, (Movie)moviePicker.getValue());
                    result.setValue(resultString);
                }
                fillMoviePicker();
                moviePicker.setVisible(true);
                moviePicker.addListener(new Property.ValueChangeListener() {
                    @Override
                    public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                        result.setVisible(true);
                        result.setCaption(functionality);
                        String resultString = getUI().service.nf2_query(functionality, (String)columnPicker.getValue(), movie, (Movie)moviePicker.getValue());
                        result.setValue(resultString);
                        resultPanel.setVisible(true);
                    }
                });
            }
        });
    }

    public void fillMoviePicker() {
        // query movies from db and add them to the picker
        HashMap<Integer, Movie> hashMap = getUI().service.getMovies();
        if(!hashMap.isEmpty()){
            System.out.println("filled movie picker.");
            for (Movie value : hashMap.values())
            {
                moviePicker.addItem(value);
            }
        }
    }

    public void resetButtons() {
        moviePicker.select(moviePicker.getNullSelectionItemId());
        resultPanel.setVisible(false);
        columnPicker.setVisible(false);
        columnPicker.select(columnPicker.getNullSelectionItemId());
        moviePicker.setVisible(false);
        result.setVisible(false);
        intersection.setEnabled(true);
        difference.setEnabled(true);
        union.setEnabled(true);
        subset.setEnabled(true);
        properSubset.setEnabled(true);
        equal.setEnabled(true);
        notEqual.setEnabled(true);
    }
}
