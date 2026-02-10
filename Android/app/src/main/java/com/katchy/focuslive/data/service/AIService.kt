package com.katchy.focuslive.data.service


import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class BrainDumpTask(
    val title: String,
    val description: String = "",
    val dueDateTimestamp: Long? = null,
    val time: String? = null, // Format HH:mm
    val estimatedMinutes: Int = 15
)

@Singleton
class AIService @Inject constructor(
    private val groqApiLazy: dagger.Lazy<com.katchy.focuslive.data.api.GroqApi>
) {
    private val gson = Gson()
    
    // Lazy access to api
    private val groqApi: com.katchy.focuslive.data.api.GroqApi
        get() = groqApiLazy.get()

    /**
     * Convierte texto libre en tareas estructuradas.
     * @param input Texto del usuario (ej: "Comprar leche y llamar a Juan mañana")
     * @return Lista de tareas estructuradas
     */
    suspend fun generateBrainDumpTasks(input: String): List<BrainDumpTask> = withContext(Dispatchers.IO) {
        val now = java.util.Calendar.getInstance()
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        val fullDateFormat = java.text.SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", java.util.Locale("es", "ES"))
        
        val todayDate = dateFormat.format(now.time)
        val currentTime = timeFormat.format(now.time)
        val readableDate = fullDateFormat.format(now.time)

        val systemPrompt = """
            ERES UN ASISTENTE DE PRODUCTIVIDAD.
            Tu misión es extraer tareas concretas del siguiente texto.
            
            CONTEXTO TEMPORAL (CRÍTICO):
            - Hoy es: $readableDate ($todayDate).
            - Hora actual: $currentTime.
            - Si el usuario dice "mañana", es el día siguiente a $todayDate.
            - Si dice "el viernes", es el próximo viernes.
            
            INSTRUCCIONES:
            1. Analiza el texto e infiere tareas accionables.
            2. Extrae fechas y horas explícitas o implícitas.
            3. Responde SOLAMENTE con un JSON Array válido.
            
            MODELO JSON:
            [
              {
                "title": "Titulo de la tarea",
                "date": "YYYY-MM-DD", 
                "time": "HH:mm", 
                "description": "Detalles extra"
              }
            ]
            
            NOTA:
            - Si no menciona fecha, usa "$todayDate".
            - Si no menciona hora, usa null.
        """.trimIndent()

        try {
            android.util.Log.d("AIService", "Requesting Groq Brain Dump for: $input")
            
            val request = com.katchy.focuslive.data.api.GroqRequest(
                model = "llama-3.3-70b-versatile",
                messages = listOf(
                    com.katchy.focuslive.data.api.GroqMessage("system", systemPrompt),
                    com.katchy.focuslive.data.api.GroqMessage("user", input)
                )
            )

            val response = groqApi.chatCompletion(request)
            val rawText = response.choices.firstOrNull()?.message?.content ?: "[]"
            val jsonText = rawText.extractJsonArray()
            
            // Temporary DTO for JSON parsing
            data class TempTaskDto(
                val title: String,
                val description: String = "",
                val date: String? = null,
                val time: String? = null,
                val estimatedMinutes: Int = 15
            )

            val type = object : TypeToken<List<TempTaskDto>>() {}.type
            val dtos: List<TempTaskDto> = gson.fromJson(jsonText, type) ?: emptyList()
            
            // Map DTO to Domain Model with Timestamp calculation
            val tasks = dtos.map { dto ->
                var timestamp: Long? = null
                if (!dto.date.isNullOrBlank()) {
                    try {
                        val parsedDate = dateFormat.parse(dto.date)
                        val cal = java.util.Calendar.getInstance()
                        parsedDate?.let { cal.time = it }
                        
                        // Set specific time if available, otherwise default to "start of day" logic or just date
                         if (!dto.time.isNullOrBlank()) {
                            val parts = dto.time.split(":")
                            if (parts.size == 2) {
                                cal.set(java.util.Calendar.HOUR_OF_DAY, parts[0].toInt())
                                cal.set(java.util.Calendar.MINUTE, parts[1].toInt())
                            }
                        } else {
                            // If no time is specified, maybe input 9:00 AM default or keep 00:00?
                            // Let's keep existing time logic or just date. 
                            // Usually PlannerViewModel expects timestamp.
                        }
                        timestamp = cal.timeInMillis
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                BrainDumpTask(
                    title = dto.title,
                    description = dto.description,
                    dueDateTimestamp = timestamp,
                    time = dto.time,
                    estimatedMinutes = dto.estimatedMinutes
                )
            }
            
            return@withContext tasks
        } catch (e: Exception) {
            android.util.Log.e("AIService", "Error generating tasks with Groq", e)
            e.printStackTrace()
            return@withContext emptyList()
        }
    }
    
    // Improved JSON Extractor - Aggressively looks for [ ... ]
    private fun String.extractJsonArray(): String {
        val firstBracket = indexOf('[')
        val lastBracket = lastIndexOf(']')
        if(firstBracket != -1 && lastBracket != -1 && lastBracket > firstBracket) {
            return substring(firstBracket, lastBracket + 1)
        }
        return "[]" // Fallback to empty list if no brackets found
    }

    /**
     * Reordena y prioriza tareas usando la Matriz de Eisenhower.
     */
    suspend fun prioritizeTasks(tasks: List<com.katchy.focuslive.data.model.Task>): List<PrioritizedTask> = withContext(Dispatchers.IO) {
        if (tasks.isEmpty()) return@withContext emptyList()

        val tasksJson = tasks.joinToString(separator = ", ") { 
             "{ \"id\": \"${it.id}\", \"title\": \"${it.title}\", \"category\": \"${it.category}\" }" 
        }

        val systemPrompt = """
            ERES UN EXPERTO EN GESTIÓN DE TIEMPO (Matriz de Eisenhower).
            Tu misión es asignar una prioridad (HIGH, MEDIUM, LOW) a cada tarea basándote en su URGENCIA e IMPORTANCIA.
            
            CRITERIOS:
            - HIGH (Urgente e Importante): Crisis, deadlines hoy, salud, pagos.
            - MEDIUM (Importante no Urgente): Planificación, ejercicio, mejoras.
            - LOW (Ni Urgente ni Importante): Entretenimiento, trivialidades.

            INPUT:
            [$tasksJson]

            INSTRUCCIONES:
            1. Analiza cada tarea.
            2. Devuelve un JSON Array con el ID y la nueva prioridad.
            3. Sé estricto. No todo puede ser HIGH.
            
            MODELO DE RESPUESTA JSON:
            [
              { "taskId": "ID_DE_LA_TAREA", "newPriority": "HIGH", "reason": "Es un pago con deadline." }
            ]
        """.trimIndent()

        try {
            android.util.Log.d("AIService", "Prioritizing ${tasks.size} tasks...")
            
            val request = com.katchy.focuslive.data.api.GroqRequest(
                model = "llama-3.3-70b-versatile",
                messages = listOf(
                    com.katchy.focuslive.data.api.GroqMessage("system", systemPrompt),
                    com.katchy.focuslive.data.api.GroqMessage("user", "Prioriza estas tareas por favor.")
                )
            )

            val response = groqApi.chatCompletion(request)
            val rawText = response.choices.firstOrNull()?.message?.content ?: "[]"
            val jsonText = rawText.extractJsonArray()
            
            val type = object : TypeToken<List<PrioritizedTask>>() {}.type
            val prioritizedTasks: List<PrioritizedTask> = gson.fromJson(jsonText, type) ?: emptyList()
            
            return@withContext prioritizedTasks
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }
    /**
     * Generates a breakdown of subtasks for a given task title.
     */
    suspend fun generateSubtasks(taskTitle: String): List<String> = withContext(Dispatchers.IO) {
        val systemPrompt = """
            ERES UN ASISTENTE DE PRODUCTIVIDAD EXPERTO EN "GTD" (Getting Things Done).
            Tu misión es dividir una tarea grande en 3 a 5 pasos pequeños, concretos y accionables.
            
            INPUT: "$taskTitle"
            
            OUTPUT ESPERADO (JSON Array de Strings):
            ["Paso 1", "Paso 2", "Paso 3"]
            
            REGLAS:
            1. Solo JSON Array. Sin markdown.
            2. Pasos cortos (max 5 palabras).
            3. Si la tarea ya es muy simple, devuelve una lista vacía [].
        """.trimIndent()

        try {
            val request = com.katchy.focuslive.data.api.GroqRequest(
                model = "llama-3.3-70b-versatile",
                messages = listOf(
                    com.katchy.focuslive.data.api.GroqMessage("system", systemPrompt),
                    com.katchy.focuslive.data.api.GroqMessage("user", "Divide esta tarea: $taskTitle")
                )
            )

            val response = groqApi.chatCompletion(request)
            val rawText = response.choices.firstOrNull()?.message?.content ?: "[]"
            val jsonText = rawText.extractJsonArray()
            
            val type = object : TypeToken<List<String>>() {}.type
            val subtasks: List<String> = gson.fromJson(jsonText, type) ?: emptyList()
            
            return@withContext subtasks
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }
}

data class PrioritizedTask(
    val taskId: String,
    val newPriority: String,
    val reason: String
)
