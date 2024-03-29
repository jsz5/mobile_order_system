package com.example.mobileordersystem

import android.content.Intent
import android.content.res.Configuration
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import androidx.fragment.app.Fragment
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.example.mobileordersystem.authorization.ChangePasswordActivity
import com.example.mobileordersystem.authorization.SignInActivity
import com.example.mobileordersystem.customer.CustomerFragment
import com.example.mobileordersystem.equipment.EquipmentFragment
import com.example.mobileordersystem.order.OrderFragment
import com.firebase.ui.auth.AuthUI
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*



class HomeActivity : AppCompatActivity() {

    val TAG = "HomeActivity"


    var nightMode = true
    var currentid = 0
    val fragments = ArrayList<androidx.fragment.app.Fragment>()
    var fragmentStack = ArrayList<Int>()
    lateinit var displayName: String
    lateinit var userEmail: String
    lateinit var drawerToggle: ActionBarDrawerToggle
    val currentUser = FirebaseAuth.getInstance().currentUser


    private val bottomNavListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
                openFragment(0, true)
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_equipment -> {
                openFragment(1, true)
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_orders -> {
                openFragment(2, true)
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_customers -> {
                openFragment(3, true)
                return@OnNavigationItemSelectedListener true
            }
            else -> {
                Toast.makeText(this, "Navigation Error", Toast.LENGTH_SHORT).show()
            }
        }
        false
    }

    private val nightModeSwitchListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        if (isChecked) {
            Log.i("switch", "on")
            delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_YES
            nightMode = true
        } else {
            Log.i("switch", "off")
            nightMode = false
            delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_NO
        }
    }

    private val drawerNavListener = NavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.passwordChange -> changePassword()
            R.id.deleteAccount -> showDeleteAccountDialog()
            R.id.logout -> logout()
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        setUpFragments()

        displayName = currentUser?.displayName.toString()
        userEmail = currentUser?.email.toString()

        val navHeader = navigation_view.getHeaderView(0)
        val nightSwitch = navHeader.findViewById<SwitchMaterial>(R.id.nightModeSwitch)
        nightSwitch.setOnCheckedChangeListener(nightModeSwitchListener)
        navHeader.findViewById<TextView>(R.id.dispNameCont).text = displayName
        navHeader.findViewById<TextView>(R.id.emailCont).text = userEmail


        drawerToggle =
            object : ActionBarDrawerToggle(this, drawer_layout, R.string.drawer_open, R.string.drawer_close) {
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                    super.onDrawerSlide(drawerView, slideOffset)
                }

                override fun onDrawerOpened(drawerView: View) {
                    super.onDrawerOpened(drawerView)
                }
            }
//        drawerToggle.isDrawerIndicatorEnabled = true
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()


        drawer_layout.openDrawer(GravityCompat.START)
        drawer_layout.closeDrawer(GravityCompat.START)

        navigation_view.setNavigationItemSelectedListener(drawerNavListener)

    }

    private fun setUpFragments() {
        val navView = nav_view

        fragments.add(HomeFragment.newInstance())
        fragments.add(EquipmentFragment.newInstance())
        fragments.add(OrderFragment.newInstance())
        fragments.add(CustomerFragment.newInstance())

//        val transaction = supportFragmentManager.beginTransaction()
//        for (fragment in fragments) {
//            transaction.add(R.id.container, fragment)
//            transaction.hide(fragment)
//        }
//        transaction.commit()

        navView.setOnNavigationItemSelectedListener(bottomNavListener)
        val homeItem = navView.menu.getItem(0)
        homeItem.isChecked = true
        showFragment(fragments[0])
//        bottomNavListener.onNavigationItemSelected(homeItem)

    }

    public fun openFragment(id: Int, addToStack: Boolean) {

        if (addToStack) {
            if (fragmentStack.contains(currentid)) {
                fragmentStack.remove(currentid)
            }
            fragmentStack.add(currentid)
        }
        if (fragmentStack.contains(id)) {
            fragmentStack.remove(id)
        }
        Log.d(TAG, fragmentStack.toString())

        doReplaceTransaction(fragments[currentid], fragments[id])

        currentid = id
    }

    private fun doReplaceTransaction(toReplace: Fragment, replacement: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, replacement)
//        transaction.addToBackStack(null)
//        transaction.hide(toReplace)
//        transaction.show(replacement)
        transaction.commit()
    }

    private fun hideAllFragments() {
        val transaction = supportFragmentManager.beginTransaction()
        for (fragment in fragments) {
            transaction.hide(fragment)
        }
        transaction.commit()
    }

    private fun showFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        drawer_layout.closeDrawer(GravityCompat.START)
        super.onSaveInstanceState(outState)
        outState.putInt("currentid", currentid)
        outState.putBoolean("drawerOpen", drawer_layout.isDrawerOpen(GravityCompat.START))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.i(TAG, currentid.toString())
        if (savedInstanceState != null) {
            currentid = savedInstanceState.getInt("currentid")
            showFragment(fragments[currentid])
            nav_view.menu.getItem(currentid).isChecked = true
        }
        Log.i(TAG, "restoring")

        if(savedInstanceState?.getBoolean("drawerOpen")!!) {
            openDrawer()
        }

    }


    fun openDrawer() {
        drawer_layout.openDrawer(GravityCompat.START)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else if (fragmentStack.isEmpty()) {
            if (currentid == 0) {
                super.onBackPressed()
            } else {
                openFragment(0, false)
                val navItem = nav_view.menu.getItem(0)
                navItem.isChecked = true
            }
        } else {
            Log.d(TAG, fragmentStack.toString())
            val backId = fragmentStack.last()
            fragmentStack.removeAt(fragmentStack.size - 1)

            openFragment(backId, false)
            val navItem = nav_view.menu.getItem(backId)
            navItem.isChecked = true

            Log.d(TAG, fragmentStack.toString())
        }
//        Log.d(TAG,supportFragmentManager.getBackStackEntryAt(0).name)

    }

    private fun logout() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                finish()
            }
        finish()
    }

    private fun deleteAccount() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.delete()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this, SignInActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
    }

    private fun changePassword() {
        val intent = Intent(this, ChangePasswordActivity::class.java)
        startActivity(intent)
    }

    private fun showDeleteAccountDialog() {
        val builder = AlertDialog.Builder(this@HomeActivity)
        builder.setTitle(R.string.delete_account)
        builder.setMessage(R.string.alertDelete)
        builder.setPositiveButton("Tak") { _, _ ->
            deleteAccount()
        }
        builder.setNegativeButton("Nie") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


}


