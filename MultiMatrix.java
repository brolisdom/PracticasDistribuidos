import java.net.*;
import java.io.*;

public class MultiMatrix {
    static int N = 8;
    static double[][] A = new double[N][N];
    static double[][] B = new double[N][N];
    static double[][] C = new double[N][N];
    static double[][] Ax = new double[N/2][N];
    static double[][] Bx = new double[N/2][N];
    static double[][] Cx = new double[N/2][N/2];

    private Socket socket = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    public void Servidor(int node, int port, String ip) {
        try {
            socket = new Socket(ip, port);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            System.out.println("En espera del cliente");
            System.out.println(in.readUTF());
            out.writeInt(node);
            for (int i=0; i<N/2; i++) for (int j=0; j<N; j++) Ax[i][j] = in.readDouble();
            for (int i=0; i<N/2; i++) for (int j=0; j<N; j++) Bx[i][j] = in.readDouble();
            for (int i=0; i<N/2; i++) for (int j=0; j<N/2; j++) for (int k=0; k<N; k++) Cx[i][j] += Ax[i][k]*Bx[j][k];
            for (int i=0; i<N/2; i++) for (int j=0; j<N/2; j++) out.writeDouble(Cx[i][j]);
            socket.close();
        } catch(UnknownHostException u) {
            System.out.println(u);
        } catch(IOException i) {
            System.out.println(i);
        }
    }

    public void Cliente(int port) {
        try {
            ServerSocket server = new ServerSocket(port);
            System.out.println("Esperando a servidores");
            initMatrix();
            int ax, bx;
            int nodos = 0;
            while (nodos < 3) {
                ax = bx = N/2;
                socket = server.accept();
                out = new DataOutputStream(socket.getOutputStream());
                in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                out.writeUTF("Cliente conectado, calculando matriz ...");
                int nodo = in.readInt();
                if (nodo == 1) ax = bx = 0;
                else if (nodo == 2) bx = 0;
                else if (nodo == 3) ax = 0;
                else return;
                System.out.println("Conectado a servidor " + nodo);
                for (int i=0; i<N/2; i++) for (int j=0; j<N; j++) out.writeDouble(A[i+ax][j]);
                for (int i=0; i<N/2; i++) for (int j=0; j<N; j++) out.writeDouble(B[i+bx][j]); 
                for (int i=0; i<N/2; i++) for (int j=0; j<N/2; j++) C[i+ax][j+bx] = in.readDouble();
                socket.close();
                nodos++;
            } getChecksum();
        } catch(IOException i) {
            System.out.println(i);
        }
    }
 
    public static void main(String args[]){
        MultiMatrix objeto = new MultiMatrix();
        if (args.length == 1) {
            String ip = "13.78.180.152";
            int nodo = Integer.valueOf(args[0]);
            if (nodo == 0) objeto.Cliente(5000);
            else objeto.Servidor(nodo, 5000, ip);
        } else{ 
            System.err.println("Uso:");
            System.err.println("java MultiMatrix <nodo>");
            System.exit(0);
        }
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