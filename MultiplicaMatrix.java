import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import java.net.*;
import java.io.*;

public class MultiplicaMatrix {
    private Socket socket = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    static int N = 1000;
    static double[][] A = new double[N][N];
    static double[][] B = new double[N][N];
    static double[][] C = new double[N][N];
    static double[][] Ax = new double[N/2][N];
    static double[][] Bx = new double[N/2][N];
    static double[][] Cx = new double[N/2][N/2];

    public void Servidor(int port, int nodo) {
        try {
            ServerSocket server = new ServerSocket(port);
            System.out.println("En espera del cliente");
            socket = server.accept();
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            System.out.println(in.readUTF());
            out.writeUTF("Conectado a servidor "+nodo);
            for (int i=0; i<N/2; i++) for (int j=0; j<N; j++) Ax[i][j] = in.readDouble();
            for (int i=0; i<N/2; i++) for (int j=0; j<N; j++) Bx[i][j] = in.readDouble();
            for (int i=0; i<N/2; i++) for (int j=0; j<N/2; j++) for (int k=0; k<N; k++) Cx[i][j] += Ax[i][k]*Bx[j][k];
            for (int i=0; i<N/2; i++) for (int j=0; j<N/2; j++) out.writeDouble(Cx[i][j]);
        socket.close();
        } catch(IOException i) {
            System.out.println(i);
        }
    }

    public void Cliente(int port, String ip) {
        try {
            Socket socket = new Socket(ip, port);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out.writeUTF("Cliente conectado, calculando matriz ...");
            System.out.println(in.readUTF());
            if (port == 5000) {
                for (int i=0; i<N/2; i++) for (int j=0; j<N; j++) out.writeDouble(A[i][j]);
                for (int i=0; i<N/2; i++) for (int j=0; j<N; j++) out.writeDouble(B[i][j]); 
                for (int i=0; i<N/2; i++) for (int j=0; j<N/2; j++) C[i][j] = in.readDouble();
            } else if (port == 4000) {
                for (int i=0; i<N/2; i++) for (int j=0; j<N; j++) out.writeDouble(A[i+N/2][j]);
                for (int i=0; i<N/2; i++) for (int j=0; j<N; j++) out.writeDouble(B[i][j]); 
                for (int i=0; i<N/2; i++) for (int j=0; j<N/2; j++) C[i+N/2][j] = in.readDouble();
            } else if (port == 3000) {
                for (int i=0; i<N/2; i++) for (int j=0; j<N; j++) out.writeDouble(A[i][j]);
                for (int i=0; i<N/2; i++) for (int j=0; j<N; j++) out.writeDouble(B[i+N/2][j]); 
                for (int i=0; i<N/2; i++) for (int j=0; j<N/2; j++) C[i][j+N/2] = in.readDouble();
            }
        socket.close();
        } catch(IOException i) {
            System.out.println(i);
        }
    }

    public static void main(String args[]) {
        MultiplicaMatrix objeto = new MultiplicaMatrix();
        int nodo = Integer.valueOf(args[0]);
        String ip = "localhost";
        if (nodo == 0) {
            initMatrix();
            System.out.println("Esperando a servidores");
            objeto.Cliente(5000, ip);
            objeto.Cliente(4000, ip);
            objeto.Cliente(3000, ip);
            getChecksum();
        }
        else if (nodo == 1) objeto.Servidor(5000, nodo);
        else if (nodo == 2) objeto.Servidor(4000, nodo);
        else if (nodo == 3) objeto.Servidor(3000, nodo);
        else System.out.println("El nodo no es valido");
    }

    public static void initMatrix() {
        // creacion de la matrices A, B, C
        for (int i=0; i<N; i++) 
            for (int j=0; j<N; j++) {
            A[i][j] = i + 5  * j;
            B[i][j] = 5 * i - j;
            C[i][j] = 0;
        } for (int i=0; i<N; i++) // B traspuesta
            for (int j=0; j<i; j++) {
            double x = B[i][j];
            B[i][j] = B[j][i];
            B[j][i] = x;
        }
    }

    public static void getChecksum() {
        // ultima multiplicacion de matrices A2*B2 = C4
        System.out.println("Cerrando conexion");
        for (int i=0; i<N/2; i++) 
            for (int j=0; j<N/2; j++) 
                for (int k=0; k<N; k++) 
                    C[i+N/2][j+N/2] += A[i+N/2][k]*B[j+N/2][k];
        // impresion de matriz A y B
        if (N != 1000) {
            System.out.println("Matriz A:");
            for (int i=0; i<N; i++) {
                for (int j=0; j<N; j++) 
                    System.out.print(A[i][j]+"\t");
                System.out.println("");
            }
            System.out.println("Matriz B:");
            for (int i=0; i<N; i++) {
                for (int j=0; j<N; j++) 
                    System.out.print(B[i][j]+"\t");
                System.out.println("");
            }
        }
        // sumatoria de valores en la matriz C
        double checksum = 0;
        if (N != 1000) System.out.println("Matriz C:");
        for (int i=0; i<N; i++) {
            for (int j=0; j<N; j++) {
                checksum +=  C[i][j];
                if (N != 1000) System.out.print(C[i][j]+"\t");
            } if (N != 1000) System.out.println("");
        } System.out.println("Resultado: "+checksum);
    }
}