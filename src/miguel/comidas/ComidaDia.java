package miguel.comidas;

public class ComidaDia {
	private Comida comida;
	private Comida cena;
	private String merienda;
	
	public ComidaDia(String comida, String cena, String merienda){
		this.comida = new Comida();
		this.comida.setPrimerPlato(comida);
		this.cena = new Comida();
		this.cena.setPrimerPlato(cena);
		this.merienda = merienda;
	}
	
	public Comida getComida() {
		return comida;
	}
	public void setComida(Comida comida) {
		this.comida = comida;
	}
	public Comida getCena() {
		return cena;
	}
	public void setCena(Comida cena) {
		this.cena = cena;
	}
	public String getMerienda() {
		return merienda;
	}
	public void setMerienda(String merienda) {
		this.merienda = merienda;
	}	
}
