package shx.cotacaodolar.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import shx.cotacaodolar.model.Moeda;
import shx.cotacaodolar.model.Periodo;

@Service
public class MoedaService {

  // o formato da data que o método recebe é "MM-dd-yyyy"
  public List<Moeda> getCotacoesPeriodo(String startDate, String endDate)
      throws IOException, ParseException {
    Periodo periodo = new Periodo(startDate, endDate);

    String urlString =
        "https://olinda.bcb.gov.br/olinda/servico/PTAX/versao/v1/odata/CotacaoDolarPeriodo(dataInicial=@dataInicial,dataFinalCotacao=@dataFinalCotacao)?%40dataInicial='" +
            periodo.getDataInicial() + "'&%40dataFinalCotacao='" + periodo.getDataFinal() +
            "'&%24format=json&%24skip=0&%24top=" + periodo.getDiasEntreAsDatasMaisUm();

    var cotacoesArray = getJsonElements(urlString);

    List<Moeda> moedasLista = new ArrayList<>();

    for (JsonElement obj : cotacoesArray) {
      Moeda moedaRef = new Moeda();
      Date data = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(
          obj.getAsJsonObject().get("dataHoraCotacao").getAsString());

      moedaRef.preco = obj.getAsJsonObject().get("cotacaoCompra").getAsDouble();
      moedaRef.data = new SimpleDateFormat("dd/MM/yyyy").format(data);
      moedaRef.hora = new SimpleDateFormat("HH:mm:ss").format(data);
      moedasLista.add(moedaRef);
    }
    return moedasLista;
  }

  public List<Moeda> getCotacaoAtual() throws IOException, ParseException {
    var formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
    var dataAtual = formatter.format(LocalDate.now());

    String urlString =
        "https://olinda.bcb.gov.br/olinda/servico/PTAX/versao/v1/odata/CotacaoDolarDia(dataCotacao=@dataCotacao)?@dataCotacao='" +
            dataAtual + "'&$top=100&$format=json";

    var cotacoesArray = getJsonElements(urlString);
    if (cotacoesArray.isEmpty()) {
      return Collections.emptyList();
    }

    Moeda moedaRef = new Moeda();
    var moedaJson = cotacoesArray.get(0).getAsJsonObject();

    Date data = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(
        moedaJson.get("dataHoraCotacao").getAsString());

    moedaRef.preco = moedaJson.get("cotacaoCompra").getAsDouble();
    moedaRef.data = new SimpleDateFormat("dd/MM/yyyy").format(data);
    moedaRef.hora = new SimpleDateFormat("HH:mm:ss").format(data);

    return Collections.singletonList(moedaRef);
  }

  public List<Moeda> getCotacoesMenoresAtual(String startDate, String endDate)
      throws IOException, ParseException {
    var atual = getCotacaoAtual();
    if (atual.isEmpty()) {
      return Collections.emptyList();
    }
    var cotacaoPeriodo = getCotacoesPeriodo(startDate, endDate);

    return cotacaoPeriodo.stream().filter(x -> x.preco < atual.get(0).preco)
        .collect(Collectors.toList());
  }

  private static JsonArray getJsonElements(final String urlString) throws IOException {
    URL url = new URL(urlString);
    HttpURLConnection request = (HttpURLConnection) url.openConnection();
    request.connect();

    JsonElement response =
        JsonParser.parseReader(new InputStreamReader((InputStream) request.getContent()));
    JsonObject rootObj = response.getAsJsonObject();

    return rootObj.getAsJsonArray("value");
  }

}
