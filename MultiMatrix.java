import java.net.*;
import java.io.*;
 
public class MultiMatrix {
    static int N = 1000;
    static long[][] A = new long[N][N];
    static long[][] B = new long[N][N];
    static long[][] C = new long[N][N];
    static long[][] AX = new long[N/2][N];
    static long[][] BX = new long[N/2][N];
    static long[][] CX = new long[N/2][N/2];

    private Socket socket = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    public void Server(int port) {
        try { // inicializa el servidor y espera las conexiones
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
                System.out.println("Conectado a servidor " + nodo); // funcion principal de nodo 0
                for (int i=0; i<N/2; i++) for (int j=0; j<N; j++) out.writeLong(A[i+ax][j]); // envia matriz Ax a nodo x
                for (int i=0; i<N/2; i++) for (int j=0; j<N; j++) out.writeLong(B[i+bx][j]); // envia matriz Bx a nodo x
                for (int i=0; i<N/2; i++) for (int j=0; j<N/2; j++) C[i+ax][j+bx] = in.readLong();  // Recibe matriz Cx
                socket.close();
                nodos++;
            } getChecksum();
        } catch(IOException i) {
            System.out.println(i);
        }
    }

    public void Client(int node, int port, String ip) {
        try { // inicializa el cliente para comunicarse con el servidor
            socket = new Socket(ip, port);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            System.out.println("En espera del cliente");
            System.out.println(in.readUTF());
            out.writeInt(node);
            // funcion principal de cada nodo:
            for (int i=0; i<N/2; i++) for (int j=0; j<N; j++) AX[i][j] = in.readLong(); // Recibe Ax del nodo 0
            for (int i=0; i<N/2; i++) for (int j=0; j<N; j++) BX[i][j] = in.readLong(); // Recibe Bx del nodo 0
            for (int i=0; i<N/2; i++) for (int j=0; j<N/2; j++) for (int k=0; k<N; k++) CX[i][j] += AX[i][k]*BX[j][k]; // multiplica Ax * Bx
            for (int i=0; i<N/2; i++) for (int j=0; j<N/2; j++) out.writeLong(CX[i][j]); // Envia resultado al nodo 0
            socket.close();
        } catch(UnknownHostException u) {
            System.out.println(u);
        } catch(IOException i) {
            System.out.println(i);
        }
    }
 
    public static void main(String args[]){
        MultiMatrix objeto = new MultiMatrix();
        if (args.length == 1) {
            String ip = "13.78.180.152";
            int nodo = Integer.valueOf(args[0]);
            if (nodo == 0) objeto.Server(5000);
            else objeto.Client(nodo, 5000, ip);
        } else{ 
            System.err.println("Uso:");
            System.err.println("java MultiMAtrix <nodo>");
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
            long x = B[i][j];
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
        long checksum = 0;
        if (N != 1000) System.out.println("Matriz C:");
        for (int i=0; i<N; i++) {
            for (int j=0; j<N; j++) {
                checksum +=  C[i][j];
                if (N != 1000) System.out.print(C[i][j]+"\t");
            } if (N != 1000) System.out.println("");
        } System.out.println("Resultado: "+checksum);
    }
}