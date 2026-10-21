
import java.sql.Connection;
import java.sql.DriverManager;

public class Conexao {
    public static Connection get() throws Exception {
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/banco_db",
            "appuser",
            "SenhaForte123"
        );
    }
}
