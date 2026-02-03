<template>
  <div style="padding: 20px; max-width: 1100px; margin: auto; font-family: sans-serif; color: #e0e0e0; background: #121212; min-height: 100vh;">
    <h1 style="text-align: center; color: #42b883;">Painel de Operadoras ANS</h1>

    <div style="background: #1e1e1e; padding: 20px; border-radius: 8px; margin-bottom: 20px; border: 1px solid #333;">
      <input v-model="filtro" placeholder="Filtrar por Nome ou CNPJ..." style="padding: 12px; width: 100%; max-width: 400px; border-radius: 4px; border: none; background: #2d2d2d; color: white;">
      <p style="margin-top: 15px;">Total de registros encontrados: <span style="color: #42b883; font-weight: bold;">{{ total }}</span></p>
    </div>

    <div style="overflow-x: auto; background: #1e1e1e; border-radius: 8px; padding: 10px;">
      <table style="width: 100%; border-collapse: collapse; text-align: left;">
        <thead>
          <tr style="border-bottom: 2px solid #333; color: #42b883;">
            <th style="padding: 12px;">Registro ANS</th>
            <th>CNPJ</th>
            <th>Razão Social</th>
            <th>UF</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="op in operadoras" :key="op.CNPJ" style="border-bottom: 1px solid #2d2d2d;">
            <td style="padding: 12px;">{{ op.Registro_ANS }}</td>
            <td>{{ op.CNPJ }}</td>
            <td>{{ op.Razao_Social }}</td>
            <td>{{ op.UF }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div style="display: flex; justify-content: center; align-items: center; margin: 20px 0;">
      <button @click="pagina--" :disabled="pagina === 1" style="padding: 8px 16px; cursor: pointer;">Anterior</button>
      <span style="margin: 0 20px;">Página {{ pagina }}</span>
      <button @click="pagina++" style="padding: 8px 16px; cursor: pointer;">Próxima</button>
    </div>

    <div v-if="loaded" style="background: white; padding: 25px; border-radius: 8px; margin-top: 40px; height: 450px;">
      <h3 style="color: #333; margin-top: 0;">Distribuição: Top 5 Estados (UF)</h3>
      <Bar :data="chartData" :options="chartOptions" />
    </div>
  </div>
</template>

<script setup>
// Importei o Axios para chamadas HTTP e os componentes do Chart.js para o gráfico.
import { ref, onMounted, watch } from 'vue'
import axios from 'axios'
import { Bar } from 'vue-chartjs'
import { Chart as ChartJS, Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale } from 'chart.js'

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale)

// Variáveis Reativas: O Vue atualiza a tela automaticamente quando esses valores mudam.
const operadoras = ref([]) // Lista que aparece na tabela
const total = ref(0)  // Contador de registros
const pagina = ref(1)  // Controle da paginação
const filtro = ref('')
const loaded = ref(false)  // Garante que o gráfico só apareça quando houver dados
const chartData = ref(null)

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { position: 'top' } }
}

const carregarDados = async () => {
  try {
    // Busca os dados da tabela
    const res = await axios.get(`http://127.0.0.1:8000/api/operadoras?page=${pagina.value}`)
    operadoras.value = res.data.data
    total.value = res.data.total
    
    // Busca as estatísticas para o gráfico
    const resStats = await axios.get('http://127.0.0.1:8000/api/estatisticas')
    const stats = resStats.data.top_5_estados
    
    chartData.value = {
      labels: Object.keys(stats),
      datasets: [{
        label: 'Quantidade de Operadoras',
        data: Object.values(stats),
        backgroundColor: '#42b883'
      }]
    }
    loaded.value = true
  } catch (e) {
    console.error("Erro ao carregar dados. Verifique se o uvicorn está rodando!")
  }
}
// O 'watch' observa a variável 'pagina'. Se você clicar em "Próxima", 
// ele chama carregarDados() automaticamente.
watch([pagina, filtro], carregarDados)
onMounted(carregarDados)
</script>