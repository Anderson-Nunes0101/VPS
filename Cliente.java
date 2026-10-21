import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Cliente {
    public static void main(String[] args) throws Exception {
        Socket s = new Socket("localhost", 5000); // troque pelo IP da AWS
        DataOutputStream out = new DataOutputStream(s.getOutputStream());
        DataInputStream in = new DataInputStream(s.getInputStream());
        Scanner sc = new Scanner(System.in);

        int op = -1;
        while (op != 0) {
            System.out.println("\nNunes Bank");
            System.out.println("1.cadastrar");
            System.out.println("2.atualizar");
            System.out.println("3.remover");
            System.out.println("4.listar");
            System.out.println("5.gerir");
            System.out.println("0.sair");
            op = Integer.parseInt(sc.nextLine());
            out.writeInt(op);

            if (op == 1) {
                System.out.print("Numero: "); out.writeInt(Integer.parseInt(sc.nextLine()));
                System.out.print("NIB: "); out.writeInt(Integer.parseInt(sc.nextLine()));
                System.out.print("PIN: "); out.writeInt(Integer.parseInt(sc.nextLine()));

            } else if (op == 2) {
                System.out.print("Numero: "); out.writeInt(Integer.parseInt(sc.nextLine()));
                System.out.print("Novo NIB: "); out.writeInt(Integer.parseInt(sc.nextLine()));
                System.out.print("Novo PIN: "); out.writeInt(Integer.parseInt(sc.nextLine()));

            } else if (op == 3) {
                System.out.print("Numero: "); out.writeInt(Integer.parseInt(sc.nextLine()));

            } else if (op == 5) {
                System.out.println("\n1.depositar");
                System.out.println("2.transferir");
                System.out.println("3.levantar");
                System.out.println("4.consultar saldo");
                int sub = Integer.parseInt(sc.nextLine());
                out.writeInt(sub);

                System.out.print("Numero da conta: "); out.writeInt(Integer.parseInt(sc.nextLine()));
                System.out.print("PIN: "); out.writeInt(Integer.parseInt(sc.nextLine()));

                if (sub == 1 || sub == 3) {
                    System.out.print("Valor: "); out.writeDouble(Double.parseDouble(sc.nextLine()));
                } else if (sub == 2) {
                    System.out.print("Numero destino: "); out.writeInt(Integer.parseInt(sc.nextLine()));
                    System.out.print("Valor: "); out.writeDouble(Double.parseDouble(sc.nextLine()));
                }
            }

            out.flush();
            if (op != 0) System.out.println("\n" + in.readUTF());
        }
        s.close();
    }
}
