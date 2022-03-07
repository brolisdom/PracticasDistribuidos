import java.rmi.Naming;

public class ClienteRMI{

  static int N = 8;

  static float[][] separa_matriz(float[][] A, int inicio){

    float[][] M =  new float[N/2][N];
    for (int i=0;i<N/2; i++)
      for(int j = 0; j< N; j++)
        M[i][j] = A[i+inicio][j];
    
    return M;
  }

  static void acomoda_matriz(float[][] C, float[][] A, int renglon, int columna){

    for (int i=0;i<N/2; i++)
      for(int j = 0; j< N/2; j++)
        C[i + renglon][j+columna] = A[i][j];
  }

  static void imprimir_matriz(float[][] A, int filas, int columnas){
    for(int i=0;i<filas;i++){
      for(int j=0; j<columnas; j++)
        System.out.print(A[i][j] + " ");
      System.out.print("\n");
    }
  }

  static double calcular_checksum(float[][] A, int filas, int columnas){
    double checksum = 0;
    for(int i = 0;i<filas;i++)
      for(int j = 0; j<columnas;j++)
        checksum = checksum + A[i][j];

    return checksum;
  }

  public static void main(String args[]) throws Exception{

    float[][] A = new float[N][N];
    float[][] B = new float[N][N];
    float[][] C = new float[N][N];
    double checksum = 0;
    // en este caso el objeto remoto se llama "prueba", notar que se utiliza el puerto default 1099
    String url = "rmi://localhost/prueba";

    // obtiene una referencia que "apunta" al objeto remoto asociado a la URL
    InterfaceRMI r = (InterfaceRMI)Naming.lookup(url);

    //Inicializar las matrices A y B
    for(int i = 0; i < N; i++)
      for(int j = 0; j < N; j++){
        A[i][j] = i + 3 * j;
        B[i][j] = i - 3 * j;
      }
    
    //Trasponer la matriz B
    for(int i = 0; i < N; i++)
      for(int j = 0; j < i; j++){
          float x = B[i][j];
          B[i][j] = B[j][i];
          B[j][i] = x;
      }

    //Obtener las matrices A1, A2, B1, B2
    float[][] A1 = separa_matriz(A, 0);
    float[][] A2 = separa_matriz(A, N/2);
    float[][] B1 = separa_matriz(B, 0);
    float[][] B2 = separa_matriz(B, N/2);

    //Obtener las matrices C1, C2, C3 y C4
    float[][] C1 = r.multiplica_matrices(A1, B1);
    float[][] C2 = r.multiplica_matrices(A1, B2);
    float[][] C3 = r.multiplica_matrices(A2, B1);
    float[][] C4 = r.multiplica_matrices(A2, B2);

    acomoda_matriz(C, C1, 0, 0);
    acomoda_matriz(C, C2, 0, N/2);
    acomoda_matriz(C, C3, N/2, 0);
    acomoda_matriz(C, C4, N/2, N/2);

    if(N == 8){
      System.out.println("      Matriz A");
      imprimir_matriz(A, N, N);
      System.out.println("\n      Matriz B");
      imprimir_matriz(B, N, N);
      System.out.println("\n      Matriz C");
      imprimir_matriz(C, N, N);
    }
    checksum = calcular_checksum(C, N, N);
    System.out.println("\nChecksum: "+ checksum);
  }
}
