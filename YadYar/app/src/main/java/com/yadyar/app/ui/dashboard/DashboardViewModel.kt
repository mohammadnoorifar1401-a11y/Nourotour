package com.yadyar.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.yadyar.app.YadyarApp
import com.yadyar.app.data.model.Member
import com.yadyar.app.data.model.MemberWithStats
import com.yadyar.app.data.repository.MemberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val memberRepository: MemberRepository,
    folderId: String? = null
) : ViewModel() {

    val members: Flow<List<MemberWithStats>> = folderId?.let(memberRepository::observeActiveWithStatsByFolder) ?: memberRepository.observeActiveWithStats()

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
        fun factory(folderId: String?): ViewModelProvider.Factory = viewModelFactory {
            initializer { val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as YadyarApp; DashboardViewModel(app.container.memberRepository, folderId) }
        }
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as YadyarApp
                DashboardViewModel(app.container.memberRepository)
            }
        }
    }
}
