package org.example;
import com.opencsv.CSVReader;
import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        String filePath = "data/movies.csv"; // Update with your file path
        List<Movie> movies = loadMovies(filePath);

        if (movies.isEmpty()) {
            System.out.println("No peliclas, checa el CSV");
            return;
        }

        System.out.println("Cargado exitosamente!");
        Scanner scanner = new Scanner(System.in);

        while (true) {
            displayMenu();
            int choice = getValidIntInput(scanner, "Elige una opcion: ");
            switch (choice) {
                case 1:
                    displayMoviesPaged(movies, scanner);
                    break;
                case 2:
                    System.out.print("Escribe nombre de pelicula: ");
                    String title = scanner.nextLine();
                    searchMovieByTitle(movies, title);
                    break;
                case 3:
                    System.out.print("Escribe el genero (e.g., Action, Drama): ");
                    String genre = scanner.nextLine();
                    recommendMoviesByGenre(movies, genre);
                    break;
                case 4:
                    double rating = getValidDoubleInput(scanner, "Escribe un minimo de rating (e.g., 8.0): ");
                    recommendTopRatedMovies(movies, rating);
                    break;
                case 5:
                    sortAndDisplayMovies(movies, scanner);
                    break;
                case 6:
                    System.out.print("Quieres salir? (yes/no): ");
                    if (scanner.nextLine().equalsIgnoreCase("yes")) {
                        System.out.println("Exiting... Goodbye!");
                        return;
                    }
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    public static void displayMenu() {
        System.out.println("\nEscoge una opcion:");
        System.out.println("1. Muestra todas las peliculas");
        System.out.println("2. Busca una pelicula por su nombre");
        System.out.println("3. Recomienda pelicula por genero");
        System.out.println("4. Recomienda peliculas top");
        System.out.println("5. Ordenar peliculas por anno o rating");
        System.out.println("6. Salir");
    }

    public static int getValidIntInput(Scanner scanner, String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalido");
            }
        }
    }

    public static double getValidDoubleInput(Scanner scanner, String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalido");
            }
        }
    }

    public static List<Movie> loadMovies(String filePath) {
        List<Movie> movies = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] line;
            reader.readNext(); // Skip header line
            while ((line = reader.readNext()) != null) {
                try {
                    String title = line[1];
                    int year = parseInteger(line[2]);
                    String genre = line[5];
                    double rating = parseDouble(line[6]);
                    String overview = line[7];

                    // Default values for missing fields
                    title = (title != null && !title.isEmpty()) ? title : "Unknown Title";
                    genre = (genre != null && !genre.isEmpty()) ? genre : "Unknown Genre";
                    overview = (overview != null && !overview.isEmpty()) ? overview : "No Overview";

                    movies.add(new Movie(title, year, genre, rating, overview));
                } catch (Exception e) {
                    System.err.println("Skipping malformed row: " + Arrays.toString(line) + " - " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading the CSV file: " + e.getMessage());
        }
        return movies;
    }

    private static int parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public static void displayMoviesPaged(List<Movie> movies, Scanner scanner) {
        final int pageSize = 10;
        int totalPages = (int) Math.ceil((double) movies.size() / pageSize);
        int currentPage = 1;

        while (true) {
            System.out.println("\nPagina " + currentPage + " de " + totalPages);
            movies.stream()
                    .skip((currentPage - 1) * pageSize)
                    .limit(pageSize)
                    .forEach(System.out::println);

            System.out.print("Pulsa 'n' para siguiente, 'p' para volver, o 'q' para salir: ");
            String input = scanner.nextLine().toLowerCase();

            if (input.equals("n") && currentPage < totalPages) {
                currentPage++;
            } else if (input.equals("p") && currentPage > 1) {
                currentPage--;
            } else if (input.equals("q")) {
                break;
            } else {
                System.out.println("Invalido.");
            }
        }
    }

    public static void sortAndDisplayMovies(List<Movie> movies, Scanner scanner) {
        System.out.println("\nEscoge order por:");
        System.out.println("1. Anno");
        System.out.println("2. Rating");
        int sortChoice = getValidIntInput(scanner, "Ecoge una opcion: ");

        List<Movie> sortedMovies = new ArrayList<>(movies);
        if (sortChoice == 1) {
            sortedMovies.sort(Comparator.comparingInt(Movie::getYear));
        } else if (sortChoice == 2) {
            sortedMovies.sort(Comparator.comparingDouble(Movie::getRating).reversed());
        } else {
            System.out.println("Invalido.");
            return;
        }

        displayMoviesPaged(sortedMovies, scanner);
    }

    public static void searchMovieByTitle(List<Movie> movies, String title) {
        List<Movie> found = movies.stream()
                .filter(movie -> movie.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());
        if (found.isEmpty()) {
            System.out.println("No peliculas encontradas con el titulo: " + title);
        } else {
            System.out.println("Pelicula encontrada:");
            found.forEach(System.out::println);
        }
    }

    public static void recommendMoviesByGenre(List<Movie> movies, String genre) {
        List<Movie> recommended = movies.stream()
                .filter(movie -> movie.getGenre().toLowerCase().contains(genre.toLowerCase()))
                .collect(Collectors.toList());
        if (recommended.isEmpty()) {
            System.out.println("No peliculas encontradas en el genero: " + genre);
        } else {
            System.out.println("Peliculas recomendadas en " + genre + " genre:");
            recommended.forEach(System.out::println);
        }
    }

    public static void recommendTopRatedMovies(List<Movie> movies, double minRating) {
        List<Movie> topRated = movies.stream()
                .filter(movie -> movie.getRating() >= minRating)
                .sorted(Comparator.comparingDouble(Movie::getRating).reversed())
                .collect(Collectors.toList());
        if (topRated.isEmpty()) {
            System.out.println("No peliculas encontradas con el rating >= " + minRating);
        } else {
            System.out.println("Peliculas Top-rated:");
            topRated.forEach(System.out::println);
        }
    }
}

class Movie {
    private final String title;   // Marked as final
    private final int year;       // Marked as final
    private final String genre;   // Marked as final
    private final double rating;  // Marked as final
    private final String overview; // Marked as final

    public Movie(String title, int year, String genre, double rating, String overview) {
        this.title = title;
        this.year = year;
        this.genre = genre;
        this.rating = rating;
        this.overview = overview;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public String getGenre() {
        return genre;
    }

    public double getRating() {
        return rating;
    }

    public String getOverview() {
        return overview;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "title='" + title + '\'' +
                ", year=" + year +
                ", genre='" + genre + '\'' +
                ", rating=" + rating +
                ", overview='" + overview + '\'' +
                '}';
    }
}


