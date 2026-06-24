import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    
    private static Stage stagePrincipal; // Guarda a janela para permitir a troca de telas

    @Override
    public void start(Stage primaryStage) throws Exception {
        stagePrincipal = primaryStage;
        
        // Configura para abrir PRIMEIRO na tela de cadastro de veículos
        // Lembre-se: O arquivo "GestaoFrotas.fxml" deve estar dentro da pasta "view" no seu projeto
        trocarTela("/view/GestaoFrotas.fxml", "Passo 1: Cadastro de Veículos");
    }

    /**
     * Método utilitário para mudar de tela dinamicamente durante a execução.
     * Como é um método static, usamos 'MainApp.class.getResource' para localizar os arquivos.
     */
    public static void trocarTela(String caminhoFxml, String titulo) {
        try {
            // 1. Carrega o arquivo FXML usando o caminho informado
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(caminhoFxml));
            Parent root = loader.load(); 
            
            // 2. Configura e exibe a nova cena na janela principal
            stagePrincipal.setTitle(titulo);
            stagePrincipal.setScene(new Scene(root, 1000, 600));
            stagePrincipal.show();
            
        } catch (Exception e) {
            System.err.println("Erro ao carregar a tela: " + caminhoFxml);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Inicia a aplicação JavaFX
        launch(args);
    }
}