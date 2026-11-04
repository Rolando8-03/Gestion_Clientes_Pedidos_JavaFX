package edu.uam.gestion.service;
import edu.uam.gestion.config.HibernateUtil;
import edu.uam.gestion.model.Pedido;
import edu.uam.gestion.repository.ClienteRepository;
import edu.uam.gestion.repository.PedidoRepository;
import java.math.BigDecimal;
import java.util.List;
public class PedidoService {
 private final ClienteRepository clientes=new ClienteRepository();
 private final PedidoRepository pedidos=new PedidoRepository();
 public List<Pedido> listar(){try(var s=HibernateUtil.factory().openSession()){return pedidos.listarConCliente(s);}}
 public void crear(Long clienteId,String descripcion,String monto){
  if(clienteId==null)throw new IllegalArgumentException("Seleccione un cliente");
  if(descripcion==null||descripcion.isBlank()||descripcion.trim().length()>150)throw new IllegalArgumentException("Descripción obligatoria (máx. 150)");
  final BigDecimal total;
  try{total=new BigDecimal(monto.trim()).setScale(2,java.math.RoundingMode.UNNECESSARY);}
  catch(Exception e){throw new IllegalArgumentException("Monto inválido. Use hasta 2 decimales");}
  if(total.signum()<=0)throw new IllegalArgumentException("Monto debe ser positivo");
  try(var s=HibernateUtil.factory().openSession()){
   var tx=s.beginTransaction();
   try{var cliente=clientes.obtener(s,clienteId);if(cliente==null)throw new IllegalArgumentException("Cliente inexistente");
    var pedido=new Pedido(descripcion.trim(),total);cliente.agregarPedido(pedido);pedidos.guardar(s,pedido);tx.commit();
   }catch(RuntimeException e){if(tx.isActive())tx.rollback();throw e;}
  }
 }
}
