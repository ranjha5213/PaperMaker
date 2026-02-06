package com.qalam.papermaker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.qalam.papermaker.R
import com.qalam.papermaker.models.Question

class QuestionAdapter(
    private var questions: List<Question>,
    private val onItemClick: (Question, Int) -> Unit
) : RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {
    
    class QuestionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvQuestion: TextView = view.findViewById(R.id.tvQuestion)
        val tvOptionA: TextView = view.findViewById(R.id.tvOptionA)
        val tvOptionB: TextView = view.findViewById(R.id.tvOptionB)
        val tvOptionC: TextView = view.findViewById(R.id.tvOptionC)
        val tvOptionD: TextView = view.findViewById(R.id.tvOptionD)
        val checkBox: CheckBox = view.findViewById(R.id.checkBox)
        val tvQuestionNumber: TextView = view.findViewById(R.id.tvQuestionNumber)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_question, parent, false)
        return QuestionViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val question = questions[position]
        
        holder.tvQuestionNumber.text = "${position + 1}."
        holder.tvQuestion.text = question.questionText
        holder.tvOptionA.text = "الف) ${question.optionA}"
        holder.tvOptionB.text = "ب) ${question.optionB}"
        holder.tvOptionC.text = "ج) ${question.optionC}"
        holder.tvOptionD.text = "د) ${question.optionD}"
        
        holder.checkBox.isChecked = question.isSelected
        
        holder.itemView.setOnClickListener {
            onItemClick(question, position)
        }
        
        holder.checkBox.setOnClickListener {
            onItemClick(question, position)
        }
        
        // Change background based on selection
        holder.itemView.setBackgroundResource(
            if (question.isSelected) R.color.selected_item
            else android.R.color.transparent
        )
    }
    
    override fun getItemCount(): Int = questions.size
    
    fun updateQuestions(newQuestions: List<Question>) {
        questions = newQuestions
        notifyDataSetChanged()
    }
    
    fun getSelectedQuestions(): List<Question> {
        return questions.filter { it.isSelected }
    }
    
    fun selectAll() {
        questions.forEach { it.isSelected = true }
        notifyDataSetChanged()
    }
    
    fun clearSelection() {
        questions.forEach { it.isSelected = false }
        notifyDataSetChanged()
    }
}
