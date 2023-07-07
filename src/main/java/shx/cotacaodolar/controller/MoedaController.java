package shx.cotacaodolar.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shx.cotacaodolar.model.Moeda;
import shx.cotacaodolar.service.MoedaService;

@RestController
@RequestMapping(value = "/")
public class MoedaController {

  @Autowired
  private MoedaService moedaService;

  @GetMapping("/moeda/{data1}&{data2}")
  public ResponseEntity<List<Moeda>> getCotacoesPeriodo(
      @PathVariable("data1") String startDate,
      @PathVariable("data2") String endDate) throws IOException, ParseException {
    return ResponseEntity.ok(moedaService.getCotacoesPeriodo(startDate, endDate));
  }

  @GetMapping("/moeda/atual")
  public ResponseEntity<List<Moeda>> getCotacaoAtual() throws IOException, ParseException {
    return ResponseEntity.ok(moedaService.getCotacaoAtual());
  }

  @GetMapping("/moeda/menorquehoje/{data1}&{data2}")
  public ResponseEntity<List<Moeda>> getCotacoesMenoresAtual(
      @PathVariable("data1") String startDate,
      @PathVariable("data2") String endDate) throws IOException, ParseException {
    return ResponseEntity.ok(moedaService.getCotacoesMenoresAtual(startDate, endDate));
  }

}
