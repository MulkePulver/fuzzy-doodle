import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.*;

class ExcepcionHabitante extends Exception {
    public ExcepcionHabitante(String message) {
        super(message);
    }
}

class HabitanteNoEncontrado extends ExcepcionHabitante {
    public HabitanteNoEncontrado(String message) {
        super(message);
    }
}

class FechaNacimientoInvalida extends ExcepcionHabitante {
    public FechaNacimientoInvalida(String message) {
        super(message);
    }
}

// Updated Comuna class with registryDate
class Comuna {
    private int id_comuna;
    private String zona;
    private int habitantes;
    private String nombre;
    private int numero;
    private LocalDate registryDate;

    public Comuna(int id_comuna, String zona, int habitantes, String nombre, int numero, LocalDate registryDate) {
        this.id_comuna = id_comuna;
        this.zona = zona;
        this.habitantes = habitantes;
        this.nombre = nombre;
        this.numero = numero;
        this.registryDate = registryDate;
    }

    public int getId_comuna() { return id_comuna; }
    public String getZona() { return zona; }
    public int getHabitantes() { return habitantes; }
    public String getNombre() { return nombre; }
    public int getNumero() { return numero; }
    public LocalDate getRegistryDate() { return registryDate; }

    @Override
    public String toString() {
        return "Comuna{" +
               "id_comuna=" + id_comuna +
               ", zona='" + zona + '\'' +
               ", habitantes=" + habitantes +
               ", nombre='" + nombre + '\'' +
               ", numero=" + numero +
               ", registryDate=" + registryDate +
               '}';
    }
}

// Interface remains the same
interface ComunaRepository {
    void addComuna(Comuna comuna);
    List<Comuna> buscarPorZona(String zona);
    Optional<Comuna> buscarPorId(int id);
    Optional<Comuna> buscarPorNombre(String nombre);
}

class ComunaRepositoryImpl implements ComunaRepository {
    private final List<Comuna> comunas = new ArrayList<>();
    private static final Logger logger = Logger.getLogger(ComunaRepositoryImpl.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ComunaRepositoryImpl() {
        leerArchivo();
    }

    @Override
    public void addComuna(Comuna comuna) {
        comunas.add(comuna);
        logger.info("Se ha añadido una nueva comuna: " + comuna);
        guardarArchivo();
    }

    @Override
    public List<Comuna> buscarPorZona(String zona) {
        return comunas.stream()
                      .filter(c -> c.getZona().equalsIgnoreCase(zona))
                      .toList();
    }

    @Override
    public Optional<Comuna> buscarPorId(int id) {
        return comunas.stream()
                      .filter(c -> c.getId_comuna() == id)
                      .findFirst();
    }

    @Override
    public Optional<Comuna> buscarPorNombre(String nombre) {
        return comunas.stream()
                      .filter(c -> c.getNombre().equalsIgnoreCase(nombre))
                      .findFirst();
    }

    private void guardarArchivo() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("comunas.txt"))) {
            for (Comuna comuna : comunas) {
                writer.println(comuna.getId_comuna() + "," + comuna.getZona() + "," +
                               comuna.getHabitantes() + "," + comuna.getNombre() + "," +
                               comuna.getNumero() + "," + comuna.getRegistryDate().format(DATE_FORMATTER));
            }
            logger.info("Se ha guardado " + comunas.size() + " comuna en la base de datos");
        } catch (IOException e) {
            logger.severe("Error intentando guardar los datos al archivo: " + e.getMessage());
        }
    }

    private void leerArchivo() {
        try (BufferedReader reader = new BufferedReader(new FileReader("comunas.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                comunas.add(new Comuna(
                    Integer.parseInt(parts[0]),
                    parts[1],
                    Integer.parseInt(parts[2]),
                    parts[3],
                    Integer.parseInt(parts[4]),
                    LocalDate.parse(parts[5], DATE_FORMATTER)
                ));
            }
            logger.info("Se han cargado " + comunas.size() + " comunas desde el archivo");
        } catch (IOException e) {
            logger.severe("Error cargando el archivo " + e.getMessage());
        }
    }
}

public class AdminComuna {
    private static final Logger logger = Logger.getLogger(AdminComuna.class.getName());

    public static void main(String[] args) {
        ComunaRepository repository = new ComunaRepositoryImpl();

        repository.addComuna(new Comuna(1, "Norte", 10000, "Los Colores", 1, LocalDate.now()));

        // Test searching
        System.out.println("Comunas en la zona Norte:");
        repository.buscarPorZona("Norte").forEach(System.out::println);

        System.out.println("\nSe ha encontrado la comuna 1:");
        repository.buscarPorId(1).ifPresentOrElse(
            System.out::println,
            () -> System.out.println("No se ha encontrado la comuna solicitada")
        );

        System.out.println("\nSe han encontrado la comuna solicitada ':");
        repository.buscarPorNombre("Comuna Nueva").ifPresentOrElse(
            System.out::println,
            () -> System.out.println("No se ha encontrado la comuna especificada")
        );

        try {
            if (repository.buscarPorId(999).isEmpty()) {
                throw new HabitanteNoEncontrado("No se ha encontrado el habitante con ID 999");
            }
        } catch (HabitanteNoEncontrado e) {
            logger.warning(e.getMessage());
        }

        try {
            throw new FechaNacimientoInvalida("Fecha de nacimiento inválida: -2/-1/-1999");
        } catch (FechaNacimientoInvalida e) {
            logger.warning(e.getMessage());
        }
    }
}