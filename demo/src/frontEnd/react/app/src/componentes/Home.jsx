import React, { useState, useEffect } from "react";
import axios from 'axios';

const Home = () => {
	const [pedidos, setPedidos] = useState([]);
	const[statusMap, setStatusMap] = useState({
		andamento: 'andamento',
		pronto: 'pronto',
		entregue: 'entregue'
	});
	
	// Carrega os pedidos ao montar o componente
	/*useEffect(() => {
		axios.get('http://localhost:8080/pedido/lista-pedidos')
			.then(response => setPedidos(response.data))
			.catch(error => console.error("Erro ao buscar pedidos: ", error));

	}, []);*/

	const buscarPedidos = async () => {
		try {
			const response = await axios.get('http://localhost:8080/pedido/lista-pedidos');
			setPedidos(response.data);
		} catch (error) {
			console.error("Erro ao buscar pedidos:", error);
		}
	};

const alterarStatus = async (pedidoId, novoStatus) => {
    try {
        await axios.post('http://localhost:8080/pedido/alterar-status', {
            pedidoId,  // ou simplesmente pedidoId
            novoStatus // adicione o status correto aqui
        });
        console.log(`Status do pedido ${pedidoId} alterado para ${novoStatus}`);
    } catch (error) {
        console.error('Erro ao alterar status:', error);
    }
};
	
	// Polling para buscar os pedidos a cada 5 segundos
	useEffect(() => {
		const intervalId = setInterval(() => {
			buscarPedidos();
		}, 20000); // 10000ms = 5 segundos

		// Limpa o intervalo quando o componente desmontar
		return () => clearInterval(intervalId);
	}, []);


	/*return (
		<div>
			<ul>
				{pedidos.map((pedido, index) => (
					<div>
						<li key={index}>
							<h1>Senha: {pedido.reference_id}</h1>
							<h2>Quantidade: {pedido.quantity} </h2>
							<h3>Descricao: {pedido.description} </h3>
							<h3>Status: {pedido.status} </h3>
						</li>
					</div>
				))}
			</ul>
		</div>
	)
};*/

  return (
    <div>
      <ul>
        {pedidos.map((pedido, index) => {
          // Define a classe com base no status do pedido
          const statusClass = statusMap[pedido.status.toLowerCase()] || '';

          return (
            <div key={index}>
              <li>
                <h1>Senha: {pedido.reference_id}</h1>
                <h2>Quantidade: {pedido.quantity}</h2>
                <h3>Descrição: {pedido.description}</h3>
                <div className="status-container">
                  <div className={`status-indicator ${statusClass}`}></div>
                  <button onClick={() => alterarStatus(pedido.reference_id, 'pronto')}>Pronto</button>
                  <button onClick={() => alterarStatus(pedido.reference_id, 'entregue')}>Entregue</button>
                </div>
              </li>
            </div>
          );
        })}
      </ul>
    </div>
  );
};

export default Home;