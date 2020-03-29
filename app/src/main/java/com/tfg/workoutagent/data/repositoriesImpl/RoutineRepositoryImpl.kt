package com.tfg.workoutagent.data.repositoriesImpl

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.tfg.workoutagent.data.repositories.RoutineRepository
import com.tfg.workoutagent.models.*
import com.tfg.workoutagent.vo.Resource
import kotlinx.coroutines.tasks.await
import java.sql.Timestamp
import java.util.*
import kotlin.collections.HashMap

class RoutineRepositoryImpl: RoutineRepository {

    override suspend fun getOwnRoutines(): Resource<MutableList<Routine>> {

        val trainerDB = FirebaseFirestore.getInstance()
            .collection("users")
            .whereEqualTo("email", FirebaseAuth.getInstance().currentUser!!.email)
            .get().await()

        val resultData = FirebaseFirestore.getInstance()
            .collection("routines")
            .whereEqualTo("trainer", trainerDB.documents[0].reference)
            .get().await()


        val routines = mutableListOf<Routine>()
        for (document in resultData){
            val customerRef  = document.get("customer")
            val trainerRef  = document.get("trainer")
            var routine = Routine()
            routine.id = document.id
            routine.startDate = document.getTimestamp("startDate")!!.toDate()
            routine.title = document.getString("title")!!

            val days = document.get("days")

            if(days is HashMap<*,*>){
                Log.i("Day", "${days.keys}")
                //iteramos por cada día
                for(dayKey in days.keys){
                    var day = Day()
                    day.name = dayKey.toString()
                    var dia = days[dayKey]
                    if(dia is HashMap<*,*>){
                        val diaAtributos = dia.keys
                        for(atributo in diaAtributos){
                            when(atributo.toString()) {
                                "completed" ->   day.completed = dia[atributo] as Boolean
                                "workingDay" -> {
                                    val tiempo = dia[atributo]
                                    if(tiempo is com.google.firebase.Timestamp){
                                        day.workingDay = tiempo.toDate()
                                    }
                                }
                                "activities" ->  {
                                    val actividades =  dia[atributo]

                                    if(actividades is HashMap<*,*>){
                                        for(actividad in actividades.keys){
                                            var routineActivity = RoutineActivity()
                                            routineActivity.name = actividad.toString()
                                            val actividadesValues = actividades[actividad]

                                            if(actividadesValues is HashMap<*,*>){
                                                for(activityAtributo in actividadesValues.keys){
                                                    when(activityAtributo.toString()) {
                                                        "exercise" -> {
                                                            val docRefAct = actividadesValues[activityAtributo]
                                                            if(docRefAct is DocumentReference){
                                                                val exerciseDoc = docRefAct.get().await()
                                                                var exerciseAct = Exercise()
                                                                exerciseAct.id = exerciseDoc.id
                                                                exerciseAct.title = exerciseDoc.getString("title")!!
                                                                exerciseAct.description = exerciseDoc.getString("description")!!
                                                                exerciseAct.photos = (exerciseDoc.get("photos") as MutableList<String>?)!!
                                                                exerciseAct.tags = (exerciseDoc.get("tags") as MutableList<String>?)!!

                                                                routineActivity.exercise = exerciseAct
                                                            }
                                                        }
                                                        "note"-> routineActivity.note = actividadesValues[activityAtributo].toString()
                                                        "repetitions" -> routineActivity.repetitions =
                                                            actividadesValues[activityAtributo] as MutableList<Int>
                                                        "set" -> routineActivity.sets =
                                                            actividadesValues[activityAtributo] as Int
                                                        "type" -> routineActivity.type = actividadesValues[activityAtributo].toString()
                                                        "weightsPerRepetition" -> routineActivity.weightsPerRepetition =
                                                            actividadesValues[activityAtributo] as MutableList<Double>

                                                    }
                                                }
                                            }
                                            day.activities.add(routineActivity)
                                        }

                                    }
                                }
                            }



                        }
                    }
                   // Log.i("Dia a añadir", "$day")
                    routine.days.add(day)
                }
            }

            if(customerRef is DocumentReference){
                val customerDoc = customerRef.get().await()

                val customer = Customer()
                customer.id = customerDoc.id
                customer.name = customerDoc.getString("name")!!
                customer.surname = customerDoc.getString("surname")!!
                customer.photo = customerDoc.getString("photo")!!
                customer.phone = customerDoc.getString("phone")!!
                customer.email = customerDoc.getString("email")!!
                routine.customer = customer
                Log.i("Customer", "$customer")
            }
            if(trainerRef is DocumentReference){
                val trainerDoc = trainerRef.get().await()
                val trainer = Trainer()
                trainer.id = trainerDoc.id
                trainer.name = trainerDoc.getString("name")!!
                trainer.surname = trainerDoc.getString("surname")!!
                trainer.photo = trainerDoc.getString("photo")!!
                trainer.phone = trainerDoc.getString("phone")!!
                trainer.email = trainerDoc.getString("email")!!
                routine.trainer = trainer
            }
            routines.add(routine)
            //Log.i("RoutineList", routines.toString())

            //trainer.id = document.id
            //Log.i("UserRepository", "${trainer.id} ${trainer.academicTitle} ${trainer.birthday} ${trainer.customers} ${trainer.dni}" +
            //        "${trainer.email} ${trainer.name} ${trainer.email} ${trainer.phone}  ${trainer.photo} ${trainer.role} ${trainer.surname} ")
        }

        //val trainer : Trainer = resultData.documents[0].toObject(Trainer::class.java)!!
        //Log.i("REPO USUARIOS", "${trainer.name}")
        return Resource.Success(routines)
    }

    override suspend fun getActivity(): Resource<MutableList<Day>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}