package praxisprojekt.dboo;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;
import praxisprojekt.dboo.backend.Movie;
import praxisprojekt.dboo.backend.MovieService;

import javax.servlet.annotation.WebServlet;

/* User Interface written in Java.
 *
 * Define the user interface shown on the Vaadin generated web page by extending the UI class.
 * By default, a new UI instance is automatically created when the page is loaded. To reuse
 * the same instance, add @PreserveOnRefresh.
 */
@Title("Praxisprojekt")
@Theme("valo")
public class MovieUI extends UI {

	// defines components
    TextField filter = new TextField();
    Grid movieList = new Grid();
    Button newMovie = new Button("Neuer Eintrag");

    MovieService service = MovieService.createService();

    // the forms represent the sidebar opening when clicking the button (insertForm) or a movie (movieForm)
    MovieForm movieForm = new MovieForm();
    InsertForm insertForm = new InsertForm();

    /* The "Main method" */
    @Override
    protected void init(VaadinRequest request) {
        configureComponents();
        buildLayout();
    }


    private void configureComponents() {
        // configure the components
        newMovie.addClickListener(e -> insertForm.edit(new Movie()));
        filter.setInputPrompt("Filme filtern...");
        filter.addTextChangeListener(e -> refreshMovies(e.getText()));

        movieList.setContainerDataSource(new BeanItemContainer<>(Movie.class));
        movieList.setColumnOrder("filmname", "jahr");
        movieList.removeColumn("id");
        movieList.removeColumn("regisseur");
        movieList.removeColumn("schauspieler");
        movieList.removeColumn("genre");
        movieList.setSelectionMode(Grid.SelectionMode.SINGLE);
        movieList.addSelectionListener(e
                -> movieForm.edit((Movie) movieList.getSelectedRow()));
        refreshMovies();
    }

    private void buildLayout() {
        // configure the layout
        HorizontalLayout actions = new HorizontalLayout(filter, newMovie);
        actions.setWidth("100%");
        filter.setWidth("100%");
        actions.setExpandRatio(filter, 1);

        VerticalLayout left = new VerticalLayout(actions, movieList);
        left.setSizeFull();
        movieList.setSizeFull();
        left.setExpandRatio(movieList, 1);

        HorizontalLayout mainLayout = new HorizontalLayout(left, movieForm, insertForm);
        mainLayout.setSizeFull();
        mainLayout.setExpandRatio(left, 1);

        setContent(mainLayout);
    }

    void refreshMovies() {
        refreshMovies(filter.getValue());
    }

    private void refreshMovies(String stringFilter) {
        movieList.setContainerDataSource(new BeanItemContainer<>(
                Movie.class, service.findAll(stringFilter)));
        movieForm.setVisible(false);
        insertForm.setVisible(false);
    }




    /*  Deployed as a Servlet. */
    @WebServlet(urlPatterns = "/*")
    @VaadinServletConfiguration(ui = MovieUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }


}
