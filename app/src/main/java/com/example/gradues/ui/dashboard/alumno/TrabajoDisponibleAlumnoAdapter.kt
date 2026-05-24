package com.example.gradues.ui.dashboard.alumno

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gradues.data.model.TrabajoDisponibleAlumnoModel
import com.example.gradues.databinding.ItemTrabajoDisponibleAlumnoBinding

class TrabajoDisponibleAlumnoAdapter(
    private var listaTrabajos: List<TrabajoDisponibleAlumnoModel>,
    private val onSeleccionarClick: (TrabajoDisponibleAlumnoModel) -> Unit
) : RecyclerView.Adapter<TrabajoDisponibleAlumnoAdapter.TrabajoViewHolder>() {

    inner class TrabajoViewHolder(
        private val binding: ItemTrabajoDisponibleAlumnoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TrabajoDisponibleAlumnoModel) {
            binding.tvNombreTrabajoItem.text = item.nombreTrabajo
            binding.tvModalidadItem.text = "Modalidad: ${item.modalidad}"
            binding.tvCicloItem.text = "Ciclo: ${item.cicloAcademico}"
            binding.tvDocenteItem.text = "Docente: ${item.docenteResponsable}"
            binding.tvEstadoTrabajoItem.text = "Estado: ${item.estadoTrabajo}"
            binding.tvCuposItem.text = item.cuposDisponiblesTexto
            binding.tvDescripcionSecundariaItem.text = item.descripcionSecundaria

            binding.btnSeleccionarTrabajoItem.setOnClickListener {
                onSeleccionarClick(item)
            }

            binding.cardItemTrabajoDisponible.setOnClickListener {
                onSeleccionarClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrabajoViewHolder {
        val binding = ItemTrabajoDisponibleAlumnoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TrabajoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrabajoViewHolder, position: Int) {
        holder.bind(listaTrabajos[position])
    }

    override fun getItemCount(): Int = listaTrabajos.size

    fun actualizarLista(nuevaLista: List<TrabajoDisponibleAlumnoModel>) {
        listaTrabajos = nuevaLista
        notifyDataSetChanged()
    }
}