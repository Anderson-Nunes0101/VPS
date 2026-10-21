import java.io.*;
import java.net.*;
import java.sql.*;

public class Servidor {
    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(5000);
        System.out.println("Nunes Bank - Servidor na porta 5000...");

        while (true) {
            Socket s = server.accept();
            new Thread(() -> atender(s)).start();
        }
    }

    static void atender(Socket s) {
        try (DataInputStream in = new DataInputStream(s.getInputStream());
             DataOutputStream out = new DataOutputStream(s.getOutputStream())) {

            int op;
            while ((op = in.readInt()) != 0) {
                Connection conn = Conexao.get();

                // ===== CRUD =====
                if (op == 1) { // CADASTRAR
                    int numero = in.readInt();
                    int nib = in.readInt();
                    int pin = in.readInt();
                    PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO conta(numero, nib, saldo, pin) VALUES (?, ?, 0, ?)");
                    ps.setInt(1, numero);
                    ps.setInt(2, nib);
                    ps.setInt(3, pin);
                    ps.executeUpdate();
                    out.writeUTF("Conta cadastrada!");

                } else if (op == 2) { // ATUALIZAR
                    int numero = in.readInt();
                    int nib = in.readInt();
                    int pin = in.readInt();
                    PreparedStatement ps = conn.prepareStatement(
                        "UPDATE conta SET nib=?, pin=? WHERE numero=?");
                    ps.setInt(1, nib);
                    ps.setInt(2, pin);
                    ps.setInt(3, numero);
                    out.writeUTF(ps.executeUpdate() > 0 ? "Atualizado!" : "Numero nao existe.");

                } else if (op == 3) { // REMOVER
                    int numero = in.readInt();
                    PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM conta WHERE numero=?");
                    ps.setInt(1, numero);
                    out.writeUTF(ps.executeUpdate() > 0 ? "Removido!" : "Numero nao existe.");

                } else if (op == 4) { // LISTAR
                    ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM conta");
                    StringBuilder sb = new StringBuilder();
                    while (rs.next()) {
                        sb.append("ID ").append(rs.getInt("id"))
                          .append(" | Numero ").append(rs.getInt("numero"))
                          .append(" | NIB ").append(rs.getInt("nib"))
                          .append(" | Saldo R$ ").append(rs.getDouble("saldo"))
                          .append("\n");
                    }
                    out.writeUTF(sb.length() == 0 ? "Nenhuma conta." : sb.toString());

                // ===== GERIR =====
                } else if (op == 5) {
                    int sub = in.readInt();
                    int numero = in.readInt();
                    int pin = in.readInt();

                    // valida PIN
                    PreparedStatement check = conn.prepareStatement(
                        "SELECT saldo FROM conta WHERE numero=? AND pin=?");
                    check.setInt(1, numero);
                    check.setInt(2, pin);
                    ResultSet rs = check.executeQuery();
                    if (!rs.next()) {
                        out.writeUTF("Numero ou PIN invalido.");
                        out.flush(); conn.close(); continue;
                    }
                    double saldo = rs.getDouble("saldo");

                    if (sub == 1) { // DEPOSITAR
                        double valor = in.readDouble();
                        PreparedStatement ps = conn.prepareStatement(
                            "UPDATE conta SET saldo = saldo + ? WHERE numero=?");
                        ps.setDouble(1, valor);
                        ps.setInt(2, numero);
                        ps.executeUpdate();
                        out.writeUTF("Deposito ok. Saldo: R$ " + (saldo + valor));

                    } else if (sub == 2) { // TRANSFERIR
                        int numeroDest = in.readInt();
                        double valor = in.readDouble();
                        if (saldo < valor) {
                            out.writeUTF("Saldo insuficiente.");
                        } else {
                            PreparedStatement p1 = conn.prepareStatement(
                                "UPDATE conta SET saldo = saldo - ? WHERE numero=?");
                            p1.setDouble(1, valor); p1.setInt(2, numero);
                            p1.executeUpdate();

                            PreparedStatement p2 = conn.prepareStatement(
                                "UPDATE conta SET saldo = saldo + ? WHERE numero=?");
                            p2.setDouble(1, valor); p2.setInt(2, numeroDest);
                            int r = p2.executeUpdate();

                            if (r > 0) out.writeUTF("Transferencia ok. Saldo: R$ " + (saldo - valor));
                            else {
                                // desfaz
                                PreparedStatement undo = conn.prepareStatement(
                                    "UPDATE conta SET saldo = saldo + ? WHERE numero=?");
                                undo.setDouble(1, valor); undo.setInt(2, numero);
                                undo.executeUpdate();
                                out.writeUTF("Conta destino nao existe.");
                            }
                        }

                    } else if (sub == 3) { // LEVANTAR
                        double valor = in.readDouble();
                        if (saldo < valor) {
                            out.writeUTF("Saldo insuficiente.");
                        } else {
                            PreparedStatement ps = conn.prepareStatement(
                                "UPDATE conta SET saldo = saldo - ? WHERE numero=?");
                            ps.setDouble(1, valor);
                            ps.setInt(2, numero);
                            ps.executeUpdate();
                            out.writeUTF("Levantamento ok. Saldo: R$ " + (saldo - valor));
                        }

                    } else if (sub == 4) { // CONSULTAR SALDO
                        out.writeUTF("Saldo: R$ " + saldo);
                    }
                }

                out.flush();
                conn.close();
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}
