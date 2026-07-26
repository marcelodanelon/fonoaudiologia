const STATUS_LABELS = {
  AGENDADO: 'Agendado',
  RECEPCIONADO: 'Recepcionado',
  ATENDIDO: 'Atendido',
  CANCELADO: 'Cancelado',
  PENDENTE: 'Pendente',
  CONCLUIDA: 'Concluída',
  EM_ANDAMENTO: 'Em Andamento',
  AGENDADA: 'Agendada',
  CANCELADA: 'Cancelada',
  CHECKIN: 'Check-in',
  PORTA: 'Demanda',
  TELEFONE: 'Telefone',
  AGENDAMENTO: 'Agendamento',
  CONSULTA: 'Consulta',
  RETORNO: 'Retorno',
  AVALIACAO: 'Avaliação',
  NORMAL: 'Normal',
  NEUROSENSORIAL: 'Neurosensorial',
  CONDUTIVA: 'Condutiva',
  MISTA: 'Mista',
};

export function statusLabel(s) {
  return STATUS_LABELS[s] || s?.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase()) || s;
}
