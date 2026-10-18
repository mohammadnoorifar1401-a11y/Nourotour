package com.mono.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mono.app.MonoApp
import com.mono.app.data.model.Member
import com.mono.app.data.model.MemberWithStats
import com.mono.app.data.repository.MemberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val memberRepository: MemberRepository
) : ViewModel() {

    val members: Flow<List<MemberWithStats>> = memberRepository.observeActiveWithStats()

    val archived: Flow<List<Member>> = memberRepository.observeArchived()

    fun archiveMember(member: Member) {
        viewModelScope.launch { memberRepository.setArchived(member.id, true) }
    }

    fun unarchiveMember(member: Member) {
        viewModelScope.launch { memberRepository.setArchived(member.id, false) }
    }

    fun deleteMember(member: Member) {
        viewModelScope.launch { memberRepository.delete(member) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MonoApp
                DashboardViewModel(app.container.memberRepository)
            }
        }
    }
}
