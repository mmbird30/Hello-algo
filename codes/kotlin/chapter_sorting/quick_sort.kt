/**
 * File: quick_sort.kt
 * Created Time: 2024-1-25
 * Author: curtishd (1023632660@qq.com)
 */

package chapter_sorting

/* 元素交换 */
fun swap(nums: IntArray, i: Int, j: Int) {
    nums[i] = nums[j].also { nums[j] = nums[i] }
}

/* 哨兵划分 */
fun partition(nums: IntArray, left: Int, right: Int): Int {
    // 以 nums[left] 为基准数
    var i = left
    var j = right
    while (i < j) {
        while (i < j && nums[j] >= nums[left])
            j--           // 从右向左找首个小于基准数的元素
        while (i < j && nums[i] <= nums[left])
            i++           // 从左向右找首个大于基准数的元素
        swap(nums, i, j)  // 交换这两个元素
    }
    swap(nums, i, left)   // 将基准数交换至两子数组的分界线
    return i              // 返回基准数的索引
}

/* 快速排序 */
fun quickSort(nums: IntArray, left: Int, right: Int) {
    // 子数组长度为 1 时终止递归
    if (left >= right) return
    // 哨兵划分
    val pivot = partition(nums, left, right)
    // 递归左子数组、右子数组
    quickSort(nums, left, pivot - 1)
    quickSort(nums, pivot + 1, right)
}

/* 选取三个候选元素的中位数 */
fun medianThree(nums: IntArray, left: Int, mid: Int, right: Int): Int {
    // 此处使用异或运算来简化代码
    // 异或规则为 0 ^ 0 = 1 ^ 1 = 0, 0 ^ 1 = 1 ^ 0 = 1
    return if ((nums[left] < nums[mid]) xor (nums[left] < nums[right])) left
    else if ((nums[mid] < nums[left]) xor (nums[mid] < nums[right])) mid
    else right
}

/* 哨兵划分（三数取中值） */
fun partitionMedian(nums: IntArray, left: Int, right: Int): Int {
    // 选取三个候选元素的中位数
    val med = medianThree(nums, left, (left + right) / 2, right)
    // 将中位数交换至数组最左端
    nums[left] = nums[med].also { nums[med] = nums[left] }
    // 以 nums[left] 为基准数
    var i = left
    var j = right
    while (i < j) {
        while (i < j && nums[j] >= nums[left])
            j--                      // 从右向左找首个小于基准数的元素
        while (i < j && nums[i] <= nums[left])
            i++                      // 从左向右找首个大于基准数的元素
        swap(nums, i, j)             // 交换这两个元素
    }
    swap(nums, i, left)              // 将基准数交换至两子数组的分界线
    return i                         // 返回基准数的索引
}

/* 快速排序 */
fun quickSortMedian(nums: IntArray, left: Int, right: Int) {
    // 子数组长度为 1 时终止递归
    if (left >= right) return
    // 哨兵划分
    val pivot = partitionMedian(nums, left, right)
    // 递归左子数组、右子数组
    quickSort(nums, left, pivot - 1)
    quickSort(nums, pivot + 1, right)
}

/* 快速排序（尾递归优化） */
fun quickSortTailCall(nums: IntArray, left: Int, right: Int) {
    // 子数组长度为 1 时终止
    var l = left
    var r = right
    while (l < r) {
        // 哨兵划分操作
        val pivot = partition(nums, l, r)
        // 对两个子数组中较短的那个执行快速排序
        if (pivot - l < r - pivot) {
            quickSort(nums, l, pivot - 1) // 递归排序左子数组
            l = pivot + 1 // 剩余未排序区间为 [pivot + 1, right]
        } else {
            quickSort(nums, pivot + 1, r) // 递归排序右子数组
            r = pivot - 1 // 剩余未排序区间为 [left, pivot - 1]
        }
    }
}

/* Driver Code */
fun main() {
    /* 快速排序 */
    val nums = intArrayOf(2, 4, 1, 0, 3, 5)
    quickSort(nums, 0, nums.size - 1)
    println("快速排序完成后 nums = ${nums.contentToString()}")

    /* 快速排序（中位基准数优化） */
    val nums1 = intArrayOf(2, 4, 1, 0, 3, 5)
    quickSortMedian(nums1, 0, nums1.size - 1)
    println("快速排序（中位基准数优化）完成后 nums1 = ${nums1.contentToString()}")

    /* 快速排序（尾递归优化） */
    val nums2 = intArrayOf(2, 4, 1, 0, 3, 5)
    quickSortTailCall(nums2, 0, nums2.size - 1)
    println("快速排序（尾递归优化）完成后 nums2 = ${nums2.contentToString()}")
}