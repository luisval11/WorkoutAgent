package com.tfg.workoutagent.presentation.ui.users.trainer.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide

import com.tfg.workoutagent.R
import com.tfg.workoutagent.data.repositoriesImpl.UserRepositoryImpl
import com.tfg.workoutagent.domain.userUseCases.DisplayCustomerTrainerUseCaseImpl
import com.tfg.workoutagent.presentation.ui.users.trainer.viewModels.DisplayCustomerTrainerViewModel
import com.tfg.workoutagent.presentation.ui.users.trainer.viewModels.DisplayCustomerTrainerViewModelFactory
import com.tfg.workoutagent.vo.Resource
import kotlinx.android.synthetic.main.fragment_display_customer_trainer.*

class DisplayCustomerTrainer : Fragment() {

    private val customerId by lazy { DisplayCustomerTrainerArgs.fromBundle(arguments!!).customerId}

    private val viewModel by lazy {
        ViewModelProvider(
            this, DisplayCustomerTrainerViewModelFactory(
                customerId,
                DisplayCustomerTrainerUseCaseImpl(UserRepositoryImpl())
            )
        ).get(DisplayCustomerTrainerViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_display_customer_trainer, container, false)
    }

     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
        setupUI()
    }

    private fun observeData(){
        viewModel.getCustomer.observe(viewLifecycleOwner, Observer {
            when(it){
                is Resource.Loading -> {
                    Toast.makeText(context, "Loading", Toast.LENGTH_SHORT).show()
                }
                is Resource.Success -> {
                    display_customer_name.text = it.data.name
                    display_customer_surname.text = it.data.surname
                    display_customer_birthday.text = it.data.birthday.toString()
                    display_customer_email.text = it.data.email
                    display_customer_phone.text = it.data.phone
                    display_customer_height.text = it.data.height.toString()
                    //TODO: Pasarle tambien el ultimo peso disponible del usuario
                    Glide.with(this).load(it.data.photo).into(circleImageViewCustomer)
                }
                is Resource.Failure -> {
                    Toast.makeText(context, "${it}", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
        })
    }

    private fun setupUI(){

    }
}
