/* USER CODE BEGIN Header */
/**
  ******************************************************************************
  * @file           : main.c
  * @brief          : Main program body
  ******************************************************************************
  * @attention
  *
  * <h2><center>&copy; Copyright (c) 2020 STMicroelectronics.
  * All rights reserved.</center></h2>
  *
  * This software component is licensed by ST under BSD 3-Clause license,
  * the "License"; You may not use this file except in compliance with the
  * License. You may obtain a copy of the License at:
  *                        opensource.org/licenses/BSD-3-Clause
  *
  ******************************************************************************
  */
/* USER CODE END Header */

/* Includes ------------------------------------------------------------------*/
#include "main.h"

/* Private includes ----------------------------------------------------------*/
/* USER CODE BEGIN Includes */
#include <time.h>
#include <stdio.h>
#include <string.h>
/* USER CODE END Includes */

/* Private typedef -----------------------------------------------------------*/
/* USER CODE BEGIN PTD */

/* USER CODE END PTD */

/* Private define ------------------------------------------------------------*/
/* USER CODE BEGIN PD */
#define ADC_Buffer_Length 1024
uint16_t ADC_Buffer[ADC_Buffer_Length];



uint16_t rx_byte;

uint32_t sampleTime;

uint16_t ADC2ReadValue;

char tx_msg[10];
typedef enum {true, false} bool;


unsigned long t1;
/* do something */
unsigned long t2;



bool canSendData = false;
bool wantsToSendData = false;
bool transmitADC = false;
bool readADC = false;
bool readAndTransmitADC2Channel1 = false;

/* USER CODE END PD */

/* Private macro -------------------------------------------------------------*/
/* USER CODE BEGIN PM */

/* USER CODE END PM */

/* Private variables ---------------------------------------------------------*/
ADC_HandleTypeDef hadc1;
ADC_HandleTypeDef hadc2;
DMA_HandleTypeDef hdma_adc1;

DAC_HandleTypeDef hdac1;
DMA_HandleTypeDef hdma_dac1_ch1;

TIM_HandleTypeDef htim2;

UART_HandleTypeDef huart2;

/* USER CODE BEGIN PV */

/* USER CODE END PV */

/* Private function prototypes -----------------------------------------------*/
void SystemClock_Config(void);
static void MX_GPIO_Init(void);
static void MX_DMA_Init(void);
static void MX_USART2_UART_Init(void);
static void MX_ADC1_Init(void);
static void MX_ADC2_Init(void);
static void MX_DAC1_Init(void);
static void MX_TIM2_Init(void);
/* USER CODE BEGIN PFP */

/* USER CODE END PFP */

/* Private user code ---------------------------------------------------------*/
/* USER CODE BEGIN 0 */

float value = 0.2;

uint32_t var;

uint32_t square_val[100];
uint32_t sine_val[100];
uint32_t triang_val[100];
#define PI 3.1415926

void get_squareval ()
{
	for (int i=0;i<100;i++)
	{
		sine_val[i] = ((sin(i*2*PI/100) + 1)*(4096/2));
		square_val[i] = ((i % 50) < 25 ? 25 : 0 )* (4096 / 25);
		triang_val[i] = (50 - abs(i % (2 * 50) - 50)) * (4096 /50);


	//((i % 50) < 25 ? 25 : 0 )* (4096 / 50);
}
}

void HAL_UART_RxCpltCallback(UART_HandleTypeDef *huart)
{
  if (huart->Instance == USART2)
  {

	  if(rx_byte=='a'){


		  readAndTransmitADC2Channel1=false;

		  wantsToSendData=true;
		  	  takeSample();

	  }else if(rx_byte=='b'){
		  readAndTransmitADC2Channel1=false;
		HAL_ADC_Stop_DMA(&hadc1);
		changeToADC1ToChannel(1);
		HAL_ADC_Start_DMA(&hadc1,(uint32_t*)ADC_Buffer,ADC_Buffer_Length);

	  }else if(rx_byte=='c'){
		  readAndTransmitADC2Channel1=false;
		HAL_ADC_Stop_DMA(&hadc1);
		changeToADC1ToChannel(2);
		HAL_ADC_Start_DMA(&hadc1,(uint32_t*)ADC_Buffer,ADC_Buffer_Length);

	  }else if (rx_byte=='d'){
		  SetMUXTo(0);
	  }else if (rx_byte=='e'){

		  SetMUXTo(1);
	  }else if (rx_byte=='f'){

		  SetMUXTo(2);
	  }else if (rx_byte=='g'){

		  SetMUXTo(3);
	  }else if (rx_byte=='h'){
		  Request_ADC2_CH2();

	  }else if(rx_byte=='i'){
		  HAL_ADC_Stop_DMA(&hadc1);
		  changeToADC2ToChannel(1);
		  readAndTransmitADC2Channel1=true;
	  }else if (rx_byte=='j'){
		  HAL_ADC_Stop_DMA(&hadc1);
		  changeToADC2ToChannel(2);
		  readAndTransmitADC2Channel1=true;
	  }else if (rx_byte=='k'){
		  Request_ADC2_CH3();

	  }else if (rx_byte=='w'){
		  HAL_DAC_Stop_DMA(&hdac1, DAC_CHANNEL_1);
	  }else if (rx_byte=='x'){
		  HAL_DAC_Stop_DMA(&hdac1, DAC_CHANNEL_1);
		  HAL_DAC_Start_DMA(&hdac1, DAC_CHANNEL_1, square_val, 100, DAC_ALIGN_12B_R);
	  }else if (rx_byte=='y'){
		  HAL_DAC_Stop_DMA(&hdac1, DAC_CHANNEL_1);
		  HAL_DAC_Start_DMA(&hdac1, DAC_CHANNEL_1, triang_val , 100, DAC_ALIGN_12B_R);
	  }else if (rx_byte=='z'){
		  HAL_DAC_Stop_DMA(&hdac1, DAC_CHANNEL_1);
		  HAL_DAC_Start_DMA(&hdac1, DAC_CHANNEL_1, sine_val, 100, DAC_ALIGN_12B_R);
	  }else if (rx_byte=='1'){
		  changeADC1PreScalar(1);
	  }else if (rx_byte=='2'){
		  changeADC1PreScalar(2);
	  }else if (rx_byte=='3'){
		  changeADC1PreScalar(4);
	  }else if (rx_byte=='4'){
		  changeADC1PreScalar(8);
	  }else if (rx_byte=='5'){
		  changeADC1PreScalar(16);
	  }else if (rx_byte=='6'){
		  changeADC1PreScalar(32);
	  }else if (rx_byte=='7'){
		  changeADC1PreScalar(64);
	  }else if (rx_byte=='8'){
		  changeADC1PreScalar(128);
	  }else if (rx_byte=='9'){
		  changeADC1PreScalar(256);
	  }

    HAL_UART_Receive_IT(&huart2, &rx_byte, 1);

  }
  //  changeADC1PreScalar(3);
}


void DMATransferComplete(DMA_HandleTypeDef *hdma);

/* USER CODE END 0 */

/**
  * @brief  The application entry point.
  * @retval int
  */
int main(void)
{
  /* USER CODE BEGIN 1 */

  /* USER CODE END 1 */
  

  /* MCU Configuration--------------------------------------------------------*/

  /* Reset of all peripherals, Initializes the Flash interface and the Systick. */
  HAL_Init();

  /* USER CODE BEGIN Init */
  CoreDebug->DEMCR |= CoreDebug_DEMCR_TRCENA_Msk;
     //  DWT->LAR = 0xC5ACCE55;
       DWT->CYCCNT = 0;
       DWT->CTRL |= DWT_CTRL_CYCCNTENA_Msk;
  /* USER CODE END Init */

  /* Configure the system clock */
  SystemClock_Config();

  /* USER CODE BEGIN SysInit */

  /* USER CODE END SysInit */

  /* Initialize all configured peripherals */
  MX_GPIO_Init();
  MX_DMA_Init();
  MX_USART2_UART_Init();
  MX_ADC1_Init();
  MX_ADC2_Init();
  MX_DAC1_Init();
  MX_TIM2_Init();
  /* USER CODE BEGIN 2 */

  HAL_UART_Receive_IT(&huart2, &rx_byte, 1);


  //HAL_DMA_RegisterCallback(&hdma_usart2_tx, HAL_DMA_XFER_CPLT_CB_ID, &DMATransferComplete);

  HAL_ADC_Start_DMA(&hadc1,(uint32_t*)ADC_Buffer,ADC_Buffer_Length);


//---------------------------------------------------------------------------------------

  HAL_TIM_Base_Start(&htim2);

  get_squareval();



  /* USER CODE END 2 */
 
 

  /* Infinite loop */
  /* USER CODE BEGIN WHILE */
  while (1)
  {


	  if(wantsToSendData&&canSendData){
		//	takeSample();
		//	canSendData=false;
		//	wantsToSendData=false;
	  }



	  if(readAndTransmitADC2Channel1==true){

			 HAL_ADC_Start(&hadc2);
			HAL_ADC_PollForConversion(&hadc2,HAL_MAX_DELAY);
			ADC2ReadValue= HAL_ADC_GetValue(&hadc2);
			 sprintf(tx_msg, "%hu\r\n",ADC2ReadValue);
			 HAL_UART_Transmit(&huart2, (uint8_t*)tx_msg, strlen(tx_msg), HAL_MAX_DELAY);

			 HAL_Delay(500);

	  }

    /* USER CODE END WHILE */

    /* USER CODE BEGIN 3 */
  }
  /* USER CODE END 3 */
}

/**
  * @brief System Clock Configuration
  * @retval None
  */
void SystemClock_Config(void)
{
  RCC_OscInitTypeDef RCC_OscInitStruct = {0};
  RCC_ClkInitTypeDef RCC_ClkInitStruct = {0};
  RCC_PeriphCLKInitTypeDef PeriphClkInit = {0};

  /** Initializes the CPU, AHB and APB busses clocks 
  */
  RCC_OscInitStruct.OscillatorType = RCC_OSCILLATORTYPE_HSI;
  RCC_OscInitStruct.HSIState = RCC_HSI_ON;
  RCC_OscInitStruct.HSICalibrationValue = RCC_HSICALIBRATION_DEFAULT;
  RCC_OscInitStruct.PLL.PLLState = RCC_PLL_ON;
  RCC_OscInitStruct.PLL.PLLSource = RCC_PLLSOURCE_HSI;
  RCC_OscInitStruct.PLL.PLLMUL = RCC_PLL_MUL4;
  if (HAL_RCC_OscConfig(&RCC_OscInitStruct) != HAL_OK)
  {
    Error_Handler();
  }
  /** Initializes the CPU, AHB and APB busses clocks 
  */
  RCC_ClkInitStruct.ClockType = RCC_CLOCKTYPE_HCLK|RCC_CLOCKTYPE_SYSCLK
                              |RCC_CLOCKTYPE_PCLK1|RCC_CLOCKTYPE_PCLK2;
  RCC_ClkInitStruct.SYSCLKSource = RCC_SYSCLKSOURCE_HSI;
  RCC_ClkInitStruct.AHBCLKDivider = RCC_SYSCLK_DIV1;
  RCC_ClkInitStruct.APB1CLKDivider = RCC_HCLK_DIV1;
  RCC_ClkInitStruct.APB2CLKDivider = RCC_HCLK_DIV1;

  if (HAL_RCC_ClockConfig(&RCC_ClkInitStruct, FLASH_LATENCY_0) != HAL_OK)
  {
    Error_Handler();
  }
  PeriphClkInit.PeriphClockSelection = RCC_PERIPHCLK_ADC12;
  PeriphClkInit.Adc12ClockSelection = RCC_ADC12PLLCLK_DIV1;
  if (HAL_RCCEx_PeriphCLKConfig(&PeriphClkInit) != HAL_OK)
  {
    Error_Handler();
  }
}

/**
  * @brief ADC1 Initialization Function
  * @param None
  * @retval None
  */
static void MX_ADC1_Init(void)
{

  /* USER CODE BEGIN ADC1_Init 0 */

  /* USER CODE END ADC1_Init 0 */

  ADC_MultiModeTypeDef multimode = {0};
  ADC_ChannelConfTypeDef sConfig = {0};

  /* USER CODE BEGIN ADC1_Init 1 */

  /* USER CODE END ADC1_Init 1 */
  /** Common config 
  */
  hadc1.Instance = ADC1;
  hadc1.Init.ClockPrescaler = ADC_CLOCK_ASYNC_DIV1;
  hadc1.Init.Resolution = ADC_RESOLUTION_12B;
  hadc1.Init.ScanConvMode = ADC_SCAN_DISABLE;
  hadc1.Init.ContinuousConvMode = ENABLE;
  hadc1.Init.DiscontinuousConvMode = DISABLE;
  hadc1.Init.ExternalTrigConvEdge = ADC_EXTERNALTRIGCONVEDGE_NONE;
  hadc1.Init.ExternalTrigConv = ADC_SOFTWARE_START;
  hadc1.Init.DataAlign = ADC_DATAALIGN_RIGHT;
  hadc1.Init.NbrOfConversion = 1;
  hadc1.Init.DMAContinuousRequests = ENABLE;
  hadc1.Init.EOCSelection = ADC_EOC_SINGLE_CONV;
  hadc1.Init.LowPowerAutoWait = DISABLE;
  hadc1.Init.Overrun = ADC_OVR_DATA_OVERWRITTEN;
  if (HAL_ADC_Init(&hadc1) != HAL_OK)
  {
    Error_Handler();
  }
  /** Configure the ADC multi-mode 
  */
  multimode.Mode = ADC_MODE_INDEPENDENT;
  if (HAL_ADCEx_MultiModeConfigChannel(&hadc1, &multimode) != HAL_OK)
  {
    Error_Handler();
  }
  /** Configure Regular Channel 
  */
  sConfig.Channel = ADC_CHANNEL_1;
  sConfig.Rank = ADC_REGULAR_RANK_1;
  sConfig.SingleDiff = ADC_SINGLE_ENDED;
  sConfig.SamplingTime = ADC_SAMPLETIME_1CYCLE_5;
  sConfig.OffsetNumber = ADC_OFFSET_NONE;
  sConfig.Offset = 0;
  if (HAL_ADC_ConfigChannel(&hadc1, &sConfig) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN ADC1_Init 2 */

  /* USER CODE END ADC1_Init 2 */

}

/**
  * @brief ADC2 Initialization Function
  * @param None
  * @retval None
  */
static void MX_ADC2_Init(void)
{

  /* USER CODE BEGIN ADC2_Init 0 */

  /* USER CODE END ADC2_Init 0 */

  ADC_ChannelConfTypeDef sConfig = {0};

  /* USER CODE BEGIN ADC2_Init 1 */

  /* USER CODE END ADC2_Init 1 */
  /** Common config 
  */
  hadc2.Instance = ADC2;
  hadc2.Init.ClockPrescaler = ADC_CLOCK_ASYNC_DIV1;
  hadc2.Init.Resolution = ADC_RESOLUTION_12B;
  hadc2.Init.ScanConvMode = ADC_SCAN_DISABLE;
  hadc2.Init.ContinuousConvMode = DISABLE;
  hadc2.Init.DiscontinuousConvMode = DISABLE;
  hadc2.Init.ExternalTrigConvEdge = ADC_EXTERNALTRIGCONVEDGE_NONE;
  hadc2.Init.ExternalTrigConv = ADC_SOFTWARE_START;
  hadc2.Init.DataAlign = ADC_DATAALIGN_RIGHT;
  hadc2.Init.NbrOfConversion = 1;
  hadc2.Init.DMAContinuousRequests = DISABLE;
  hadc2.Init.EOCSelection = ADC_EOC_SINGLE_CONV;
  hadc2.Init.LowPowerAutoWait = DISABLE;
  hadc2.Init.Overrun = ADC_OVR_DATA_OVERWRITTEN;
  if (HAL_ADC_Init(&hadc2) != HAL_OK)
  {
    Error_Handler();
  }
  /** Configure Regular Channel 
  */
  sConfig.Channel = ADC_CHANNEL_2;
  sConfig.Rank = ADC_REGULAR_RANK_1;
  sConfig.SingleDiff = ADC_SINGLE_ENDED;
  sConfig.SamplingTime = ADC_SAMPLETIME_1CYCLE_5;
  sConfig.OffsetNumber = ADC_OFFSET_NONE;
  sConfig.Offset = 0;
  if (HAL_ADC_ConfigChannel(&hadc2, &sConfig) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN ADC2_Init 2 */

  /* USER CODE END ADC2_Init 2 */

}

/**
  * @brief DAC1 Initialization Function
  * @param None
  * @retval None
  */
static void MX_DAC1_Init(void)
{

  /* USER CODE BEGIN DAC1_Init 0 */

  /* USER CODE END DAC1_Init 0 */

  DAC_ChannelConfTypeDef sConfig = {0};

  /* USER CODE BEGIN DAC1_Init 1 */

  /* USER CODE END DAC1_Init 1 */
  /** DAC Initialization 
  */
  hdac1.Instance = DAC1;
  if (HAL_DAC_Init(&hdac1) != HAL_OK)
  {
    Error_Handler();
  }
  /** DAC channel OUT1 config 
  */
  sConfig.DAC_Trigger = DAC_TRIGGER_T2_TRGO;
  sConfig.DAC_OutputBuffer = DAC_OUTPUTBUFFER_ENABLE;
  if (HAL_DAC_ConfigChannel(&hdac1, &sConfig, DAC_CHANNEL_1) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN DAC1_Init 2 */

  /* USER CODE END DAC1_Init 2 */

}

/**
  * @brief TIM2 Initialization Function
  * @param None
  * @retval None
  */
static void MX_TIM2_Init(void)
{

  /* USER CODE BEGIN TIM2_Init 0 */

  /* USER CODE END TIM2_Init 0 */

  TIM_ClockConfigTypeDef sClockSourceConfig = {0};
  TIM_MasterConfigTypeDef sMasterConfig = {0};

  /* USER CODE BEGIN TIM2_Init 1 */

  /* USER CODE END TIM2_Init 1 */
  htim2.Instance = TIM2;
  htim2.Init.Prescaler = 72-1;
  htim2.Init.CounterMode = TIM_COUNTERMODE_UP;
  htim2.Init.Period = 100-1;
  htim2.Init.ClockDivision = TIM_CLOCKDIVISION_DIV1;
  htim2.Init.AutoReloadPreload = TIM_AUTORELOAD_PRELOAD_DISABLE;
  if (HAL_TIM_Base_Init(&htim2) != HAL_OK)
  {
    Error_Handler();
  }
  sClockSourceConfig.ClockSource = TIM_CLOCKSOURCE_INTERNAL;
  if (HAL_TIM_ConfigClockSource(&htim2, &sClockSourceConfig) != HAL_OK)
  {
    Error_Handler();
  }
  sMasterConfig.MasterOutputTrigger = TIM_TRGO_UPDATE;
  sMasterConfig.MasterSlaveMode = TIM_MASTERSLAVEMODE_DISABLE;
  if (HAL_TIMEx_MasterConfigSynchronization(&htim2, &sMasterConfig) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN TIM2_Init 2 */

  /* USER CODE END TIM2_Init 2 */

}

/**
  * @brief USART2 Initialization Function
  * @param None
  * @retval None
  */
static void MX_USART2_UART_Init(void)
{

  /* USER CODE BEGIN USART2_Init 0 */

  /* USER CODE END USART2_Init 0 */

  /* USER CODE BEGIN USART2_Init 1 */

  /* USER CODE END USART2_Init 1 */
  huart2.Instance = USART2;
  huart2.Init.BaudRate = 256000 ;
  huart2.Init.WordLength = UART_WORDLENGTH_8B;
  huart2.Init.StopBits = UART_STOPBITS_1;
  huart2.Init.Parity = UART_PARITY_NONE;
  huart2.Init.Mode = UART_MODE_TX_RX;
  huart2.Init.HwFlowCtl = UART_HWCONTROL_NONE;
  huart2.Init.OverSampling = UART_OVERSAMPLING_16;
  huart2.Init.OneBitSampling = UART_ONE_BIT_SAMPLE_DISABLE;
  huart2.AdvancedInit.AdvFeatureInit = UART_ADVFEATURE_NO_INIT;
  if (HAL_UART_Init(&huart2) != HAL_OK)
  {
    Error_Handler();
  }
  /* USER CODE BEGIN USART2_Init 2 */

  /* USER CODE END USART2_Init 2 */

}

/** 
  * Enable DMA controller clock
  */
static void MX_DMA_Init(void) 
{

  /* DMA controller clock enable */
  __HAL_RCC_DMA1_CLK_ENABLE();

  /* DMA interrupt init */
  /* DMA1_Channel1_IRQn interrupt configuration */
  HAL_NVIC_SetPriority(DMA1_Channel1_IRQn, 0, 0);
  HAL_NVIC_EnableIRQ(DMA1_Channel1_IRQn);
  /* DMA1_Channel3_IRQn interrupt configuration */
  HAL_NVIC_SetPriority(DMA1_Channel3_IRQn, 0, 0);
  HAL_NVIC_EnableIRQ(DMA1_Channel3_IRQn);

}

/**
  * @brief GPIO Initialization Function
  * @param None
  * @retval None
  */
static void MX_GPIO_Init(void)
{
  GPIO_InitTypeDef GPIO_InitStruct = {0};

  /* GPIO Ports Clock Enable */
  __HAL_RCC_GPIOF_CLK_ENABLE();
  __HAL_RCC_GPIOA_CLK_ENABLE();
  __HAL_RCC_GPIOB_CLK_ENABLE();

  /*Configure GPIO pin Output Level */
  HAL_GPIO_WritePin(GPIOA, GPIO_PIN_10, GPIO_PIN_RESET);

  /*Configure GPIO pin Output Level */
  HAL_GPIO_WritePin(GPIOB, GPIO_PIN_4|GPIO_PIN_5, GPIO_PIN_RESET);

  /*Configure GPIO pin : PA10 */
  GPIO_InitStruct.Pin = GPIO_PIN_10;
  GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
  GPIO_InitStruct.Pull = GPIO_NOPULL;
  GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_LOW;
  HAL_GPIO_Init(GPIOA, &GPIO_InitStruct);

  /*Configure GPIO pins : PB4 PB5 */
  GPIO_InitStruct.Pin = GPIO_PIN_4|GPIO_PIN_5;
  GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
  GPIO_InitStruct.Pull = GPIO_NOPULL;
  GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_LOW;
  HAL_GPIO_Init(GPIOB, &GPIO_InitStruct);

}

/* USER CODE BEGIN 4 */
void Request_ADC2_CH2(){
	//uint16_t value1 = 0;
	uint16_t value2= 0;


	//changeToADC2ToChannel(1);

	//HAL_ADC_Start(&hadc2);
	//HAL_ADC_PollForConversion(&hadc2,HAL_MAX_DELAY);
	//value1= HAL_ADC_GetValue(&hadc2);

	changeToADC2ToChannel(2);

	HAL_ADC_Start(&hadc2);
	HAL_ADC_PollForConversion(&hadc2,HAL_MAX_DELAY);
	value2= HAL_ADC_GetValue(&hadc2);

//	 HAL_Delay(10);

	 sprintf(tx_msg, "%hu\r\n",value2);
	 HAL_UART_Transmit(&huart2, (uint8_t*)tx_msg, strlen(tx_msg), HAL_MAX_DELAY);

	 //HAL_Delay(1000);

}

void Request_ADC2_CH3(){
	uint16_t value1 = 0;

	changeToADC2ToChannel(3);

	HAL_ADC_Start(&hadc2);
	HAL_ADC_PollForConversion(&hadc2,HAL_MAX_DELAY);
	value1= HAL_ADC_GetValue(&hadc2);



//	 HAL_Delay(10);

	 sprintf(tx_msg, "%hu\r\n",value1);
	 HAL_UART_Transmit(&huart2, (uint8_t*)tx_msg, strlen(tx_msg), HAL_MAX_DELAY);

	 //HAL_Delay(1000);

}

void HAL_ADC_ConvHalfCpltCallback(ADC_HandleTypeDef* hadc){

	//HAL_GPIO_WritePin(GPIOA, GPIO_PIN_10, GPIO_PIN_RESET);
//

}

void HAL_ADC_ConvCpltCallback(ADC_HandleTypeDef* hadc){
	if(wantsToSendData){
	//canSendData=true;
	}
	//HAL_GPIO_WritePin(GPIOA, GPIO_PIN_10, GPIO_PIN_SET);
	//HAL_ADC_Stop_DMA(&hadc1);
	 //if(transmitADC){
		 //HAL_GPIO_TogglePin(GPIOA, GPIO_PIN_10);
		 /*for(int i=0;i<ADC_BUF_LEN;i++){
					  sprintf(msg, "%hu\r\n", adc_buf[i]);
					  HAL_UART_Transmit(&huart2, (uint8_t*)msg, strlen(msg), HAL_MAX_DELAY);
		  }*/
		 /*
		  sprintf(msg, "%c\r\n", 'f');
			  HAL_UART_Transmit(&huart2, (uint8_t*)msg, strlen(msg), HAL_MAX_DELAY);

			  transmitADC=false;
	 */
	 //}
	 //HAL_Delay(200);
	 //HAL_ADC_Start_DMA(&hadc1,(uint32_t*)adc_buf,ADC_BUF_LEN);




	HAL_ADC_Stop_DMA(&hadc1);

	t2 = DWT->CYCCNT;
	unsigned long diff = t2 - t1;


	//timeDifference = timeDifference/8000000;     SystemCoreClock
	//double TD = timeDifference;
	 for(int i=0;i<ADC_Buffer_Length;i++){
					  sprintf(tx_msg, "%hu\r\n", ADC_Buffer[i]);
					  HAL_UART_Transmit(&huart2, (uint8_t*)tx_msg, strlen(tx_msg), HAL_MAX_DELAY);

	 }

		 sprintf(tx_msg, "%c,%d\r\n", 'f',diff);
		 HAL_UART_Transmit(&huart2, (uint8_t*)tx_msg, strlen(tx_msg), HAL_MAX_DELAY);


}


void DMATransferComplete(DMA_HandleTypeDef *hdma){

	huart2.Instance->CR3 &= ~USART_CR3_DMAT;

}

void takeSample(){
	 //time1 = HAL_GetTick();
	t1 = DWT->CYCCNT;
	// sampleTime=HAL_GetTick();
	  HAL_ADC_Start_DMA(&hadc1,(uint32_t*)ADC_Buffer,ADC_Buffer_Length);






}

void SetMUXTo(int i){
	if(i==0){
		HAL_GPIO_WritePin(GPIOB, GPIO_PIN_4,GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOB, GPIO_PIN_5,GPIO_PIN_RESET);
	}else if (i==1){
		HAL_GPIO_WritePin(GPIOB, GPIO_PIN_4,GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOB, GPIO_PIN_5,GPIO_PIN_RESET);
	}else if (i==2){
		HAL_GPIO_WritePin(GPIOB, GPIO_PIN_4,GPIO_PIN_RESET);
		HAL_GPIO_WritePin(GPIOB, GPIO_PIN_5,GPIO_PIN_SET);
	}else if (i==3){
		HAL_GPIO_WritePin(GPIOB, GPIO_PIN_4,GPIO_PIN_SET);
		HAL_GPIO_WritePin(GPIOB, GPIO_PIN_5,GPIO_PIN_SET);
	}
}

void changeToADC1ToChannel(int channel){
	  ADC_ChannelConfTypeDef sConfig = {0};
	  if(channel==1){
		  sConfig.Channel = ADC_CHANNEL_1;
	  }else {
		  sConfig.Channel = ADC_CHANNEL_2;
	  }
	  sConfig.Rank = ADC_REGULAR_RANK_1;
	  sConfig.SingleDiff = ADC_SINGLE_ENDED;
	  sConfig.SamplingTime = ADC_SAMPLETIME_1CYCLE_5;
	  sConfig.OffsetNumber = ADC_OFFSET_NONE;
	  sConfig.Offset = 0;
	  if (HAL_ADC_ConfigChannel(&hadc1, &sConfig) != HAL_OK)
	  {
	    Error_Handler();
	  }

}



void changeToADC2ToChannel(int channel){
	  ADC_ChannelConfTypeDef sConfig = {0};
	  if(channel==1){
	 		  sConfig.Channel = ADC_CHANNEL_1;
	 	  }else if(channel==2) {
	 		  sConfig.Channel = ADC_CHANNEL_2;
	 	  }else{
	 		  sConfig.Channel = ADC_CHANNEL_3;

	 	  }
	  sConfig.Rank = ADC_REGULAR_RANK_1;
	  sConfig.SingleDiff = ADC_SINGLE_ENDED;
	  sConfig.SamplingTime = ADC_SAMPLETIME_1CYCLE_5;
	  sConfig.OffsetNumber = ADC_OFFSET_NONE;
	  sConfig.Offset = 0;
	  if (HAL_ADC_ConfigChannel(&hadc2, &sConfig) != HAL_OK)
	  {
	    Error_Handler();
	  }
	  /* USER CODE BEGIN ADC2_Init 2 */

	  /* USER CODE END ADC2_Init 2 */


}

void changeADC1PreScalar(int i){


	  RCC_OscInitTypeDef RCC_OscInitStruct = {0};
	  RCC_ClkInitTypeDef RCC_ClkInitStruct = {0};
	  RCC_PeriphCLKInitTypeDef PeriphClkInit = {0};

	  /** Initializes the CPU, AHB and APB busses clocks
	  */
	  RCC_OscInitStruct.OscillatorType = RCC_OSCILLATORTYPE_HSI;
	  RCC_OscInitStruct.HSIState = RCC_HSI_ON;
	  RCC_OscInitStruct.HSICalibrationValue = RCC_HSICALIBRATION_DEFAULT;
	  RCC_OscInitStruct.PLL.PLLState = RCC_PLL_ON;
	  RCC_OscInitStruct.PLL.PLLSource = RCC_PLLSOURCE_HSI;
	  RCC_OscInitStruct.PLL.PLLMUL = RCC_PLL_MUL4;
	  if (HAL_RCC_OscConfig(&RCC_OscInitStruct) != HAL_OK)
	  {
	    Error_Handler();
	  }
	  /** Initializes the CPU, AHB and APB busses clocks
	  */
	  RCC_ClkInitStruct.ClockType = RCC_CLOCKTYPE_HCLK|RCC_CLOCKTYPE_SYSCLK
	                              |RCC_CLOCKTYPE_PCLK1|RCC_CLOCKTYPE_PCLK2;
	  RCC_ClkInitStruct.SYSCLKSource = RCC_SYSCLKSOURCE_HSI;
	  RCC_ClkInitStruct.AHBCLKDivider = RCC_SYSCLK_DIV1;
	  RCC_ClkInitStruct.APB1CLKDivider = RCC_HCLK_DIV1;
	  RCC_ClkInitStruct.APB2CLKDivider = RCC_HCLK_DIV1;

	  if (HAL_RCC_ClockConfig(&RCC_ClkInitStruct, FLASH_LATENCY_0) != HAL_OK)
	  {
	    Error_Handler();
	  }
	  PeriphClkInit.PeriphClockSelection = RCC_PERIPHCLK_ADC12;





	  if(i==1){
		  PeriphClkInit.Adc12ClockSelection = RCC_ADC12PLLCLK_DIV1;
	  }else if (i==2){
		  PeriphClkInit.Adc12ClockSelection = RCC_ADC12PLLCLK_DIV2;
	  }else if (i==4){
		  PeriphClkInit.Adc12ClockSelection = RCC_ADC12PLLCLK_DIV4;
	  }else if (i==8){
		  PeriphClkInit.Adc12ClockSelection = RCC_ADC12PLLCLK_DIV8;
	  }else if (i==16){
		  PeriphClkInit.Adc12ClockSelection = RCC_ADC12PLLCLK_DIV16;
	  }else if (i==32){
		  PeriphClkInit.Adc12ClockSelection = RCC_ADC12PLLCLK_DIV32;
	  }else if (i==64){
		  PeriphClkInit.Adc12ClockSelection = RCC_ADC12PLLCLK_DIV64;
	  }else if (i==128){
		  PeriphClkInit.Adc12ClockSelection = RCC_ADC12PLLCLK_DIV128;
	  }else if (i==256){
		  PeriphClkInit.Adc12ClockSelection = RCC_ADC12PLLCLK_DIV256;
	  }else{
		  PeriphClkInit.Adc12ClockSelection = RCC_ADC12PLLCLK_DIV1;
	  }






	  if (HAL_RCCEx_PeriphCLKConfig(&PeriphClkInit) != HAL_OK)
	  {
	    Error_Handler();
	  }

}
//}
//add this back in
/*
void USART2_IRQHandler(void)
{
  HAL_UART_IRQHandler(&huart2);
}
*/
/* USER CODE END 4 */

/**
  * @brief  This function is executed in case of error occurrence.
  * @retval None
  */
void Error_Handler(void)
{
  /* USER CODE BEGIN Error_Handler_Debug */
  /* User can add his own implementation to report the HAL error return state */

  /* USER CODE END Error_Handler_Debug */
}

#ifdef  USE_FULL_ASSERT
/**
  * @brief  Reports the name of the source file and the source line number
  *         where the assert_param error has occurred.
  * @param  file: pointer to the source file name
  * @param  line: assert_param error line source number
  * @retval None
  */
void assert_failed(char *file, uint32_t line)
{ 
  /* USER CODE BEGIN 6 */
  /* User can add his own implementation to report the file name and line number,
     tex: printf("Wrong parameters value: file %s on line %d\r\n", file, line) */
  /* USER CODE END 6 */
}
#endif /* USE_FULL_ASSERT */

/************************ (C) COPYRIGHT STMicroelectronics *****END OF FILE****/
