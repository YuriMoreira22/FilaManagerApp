import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

import java.io.FileReader;
import java.io.BufferedReader;
import java.awt.Desktop;


public class FileManagerApp {

// Scanner para ler entradas do usuário
private static final Scanner scanner = new Scanner(System.in);

// Caminho do arquivo de log (log.txt na mesma pasta do programa)
private static final String LOG_PATH = "log.txt";

// Método principal
public static void main(String[] args) {
int opcao;

// Mostra o nome do usuário logado no sistema operacional
System.out.println("Usuário logado: " + System.getProperty("user.name"));

// Loop principal do menu
do {
// Exibe as opções disponíveis para o usuário
System.out.println("\n=== MENU PRINCIPAL ===");
System.out.println("1 - Abrir arquivos/pastas");
System.out.println("2 - Renomear arquivo/pasta");
System.out.println("3 - Mover arquivo");
System.out.println("4 - Copiar arquivo");
System.out.println("5 - Excluir arquivo/pasta");
System.out.println("0 - Sair");
System.out.print("Escolha uma opção: ");

// Lê a opção escolhida
opcao = scanner.nextInt();
scanner.nextLine(); // Limpa o buffer do scanner

// Executa a opção escolhida
switch (opcao) {
case 1 -> abrir(); // Abrir pastas/arquivos
case 2 -> renomear(); // Renomear arquivos ou pastas
case 3 -> mover(); // Mover arquivos
case 4 -> copiar(); // Copiar arquivos
case 5 -> excluir(); // Excluir arquivos ou pastas
case 0 -> System.out.println("Encerrando...");
default -> System.out.println("Opção inválida.");
}

} while (opcao != 0); // Sai do loop quando a opção for 0 (sair)

scanner.close(); // Fecha o scanner
}

// ========================
// 1. ABRIR ARQUIVOS/PASTAS
// ========================
public static void abrir() {
String userHome = System.getProperty("user.home");

// Pastas padrão a serem exibidas
Path[] pastasPadrao = {
Paths.get(userHome, "Documents"),
Paths.get(userHome, "Pictures"),
Paths.get(userHome, "Videos"),
Paths.get(userHome, "Downloads")
};

// Exibe as pastas padrão
System.out.println("\nPastas padrão:");
for (Path p : pastasPadrao) {
System.out.println("- " + p.toString());
}

// Usuário escolhe uma pasta para abrir
System.out.print("\nDigite o caminho da pasta que deseja abrir: ");
String caminho = scanner.nextLine();

// Lista os arquivos da pasta escolhida
try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(caminho))) {
System.out.println("\nArquivos encontrados:");
for (Path file : stream) {
System.out.println(file.getFileName());
}

// Usuário pode escolher abrir um arquivo para ler o conteúdo
System.out.print("Digite o nome do arquivo para visualizar (ou ENTER para pular): ");
String arquivo = scanner.nextLine();
if (!arquivo.isEmpty()) {
Path arquivoSelecionado = Paths.get(caminho, arquivo);
if (Files.exists(arquivoSelecionado)) {
// Pergunta se o usuário quer abrir com o programa padrão
System.out.print("Deseja abrir com o programa padrão do sistema? (s/n): ");
String abrirComSistema = scanner.nextLine().toLowerCase();

if (abrirComSistema.equals("s")) {
try {
Desktop.getDesktop().open(arquivoSelecionado.toFile());
} catch (IOException e) {
System.out.println("Erro ao abrir o arquivo com o aplicativo padrão: " + e.getMessage());
}
} else {
// Lê e exibe o conteúdo do arquivo texto
try (BufferedReader reader = new BufferedReader(new FileReader(arquivoSelecionado.toFile()))) {
System.out.println("\nConteúdo do arquivo:");
String linha;
while ((linha = reader.readLine()) != null) {
System.out.println(linha);
}
}
}}}

// Registra ação no log
registrarLog("ABRIR", "Acessado: " + caminho);
} catch (IOException e) {
System.out.println("Erro: " + e.getMessage());
}
}

// ==========================
// 2. RENOMEAR ARQUIVO/PASTA
// ==========================
public static void renomear() {
System.out.print("Digite o caminho atual do arquivo ou pasta: ");
Path origem = Paths.get(scanner.nextLine());

System.out.print("Digite o novo nome: ");
String novoNome = scanner.nextLine();

try {
Path destino = origem.resolveSibling(novoNome); // Novo caminho com o novo nome
Files.move(origem, destino); // Renomeia (move com novo nome)
System.out.println("Renomeado com sucesso!");
registrarLog("RENOMEAR", origem + " -> " + destino);
} catch (IOException e) {
System.out.println("Erro ao renomear: " + e.getMessage());
}
}

// ====================
// 3. MOVER ARQUIVO
// ====================
public static void mover() {
System.out.print("Digite o caminho do arquivo a ser movido: ");
Path origem = Paths.get(scanner.nextLine());

System.out.print("Digite o caminho do diretório de destino: ");
Path destino = Paths.get(scanner.nextLine());

try {
Files.move(origem, destino.resolve(origem.getFileName())); // Move o arquivo para o destino
System.out.println("Movido com sucesso!");
registrarLog("MOVER", origem + " -> " + destino);
} catch (IOException e) {
System.out.println("Erro ao mover: " + e.getMessage());
}
}

// ====================
// 4. COPIAR ARQUIVO
// ====================
public static void copiar() {
System.out.print("Digite o caminho do arquivo a ser copiado: ");
Path origem = Paths.get(scanner.nextLine());

System.out.print("Deseja copiar para a mesma pasta? (s/n): ");
String mesmaPasta = scanner.nextLine().trim().toLowerCase();

Path destino;

if (mesmaPasta.equals("s")) {
// Copiar para a mesma pasta com nome modificado
String nomeOriginal = origem.getFileName().toString();
String nomeNovo = gerarNomeCopia1(nomeOriginal);
destino = origem.getParent().resolve(nomeNovo);
} else {
// Copiar para pasta informada pelo usuário
System.out.print("Digite o caminho do diretório de destino: ");
Path destinoPasta = Paths.get(scanner.nextLine());
destino = destinoPasta.resolve(origem.getFileName());
}

try {
Files.copy(origem, destino, StandardCopyOption.REPLACE_EXISTING);
System.out.println("Copiado com sucesso para: " + destino);
registrarLog("COPIAR", origem + " -> " + destino);
} catch (IOException e) {
System.out.println("Erro ao copiar: " + e.getMessage());
}
}

private static String gerarNomeCopia1(String nomeOriginal) {
int ponto = nomeOriginal.lastIndexOf(".");
if (ponto == -1) {
return nomeOriginal + "_copia";
} else {
String base = nomeOriginal.substring(0, ponto);
String extensao = nomeOriginal.substring(ponto);
return base + "_copia" + extensao;
}
}


private static String gerarNomeCopia(String nomeOriginal) {
// TODO Auto-generated method stub
return null;
}

// ==========================
// 5. EXCLUIR ARQUIVO/PASTA
// ==========================
public static void excluir() {
String userHome = System.getProperty("user.home");

// Pastas padrão
Path[] pastasPadrao = {
Paths.get(userHome, "Documents"),
Paths.get(userHome, "Pictures"),
Paths.get(userHome, "Videos"),
Paths.get(userHome, "Downloads")
};

// Mostra as pastas padrão
System.out.println("\nPastas padrão:");
for (Path p : pastasPadrao) {
System.out.println("- " + p.toString());
}

System.out.print("Digite o caminho do arquivo ou pasta que deseja excluir: ");
Path caminho = Paths.get(scanner.nextLine());

try {
// Se for pasta, exclui tudo dentro primeiro
if (Files.isDirectory(caminho)) {
Files.walk(caminho)
.sorted(Comparator.reverseOrder()) // Exclui os arquivos antes da pasta
.forEach(p -> {
try {
Files.delete(p);
} catch (IOException e) {
System.out.println("Erro ao excluir " + p + ": " + e.getMessage());
}
});
} else {
Files.deleteIfExists(caminho); // Exclui arquivo
}
System.out.println("Excluído com sucesso!");
registrarLog("EXCLUIR", caminho.toString());
} catch (IOException e) {
System.out.println("Erro ao excluir: " + e.getMessage());
}
}

// ====================
// REGISTRO DE LOG
// ====================
public static void registrarLog(String acao, String detalhes) {
try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_PATH, true))) {
writer.write(LocalDateTime.now() + " | " + acao + " | " + detalhes);
writer.newLine(); // Nova linha no log
} catch (IOException e) {
System.out.println("Erro ao registrar log: " + e.getMessage());
}
}
}